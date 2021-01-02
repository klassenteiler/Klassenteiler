import { Injectable } from '@angular/core';
import * as forge from 'node-forge';
import { ClassTeacherT, ClearLocalStudentI, SchoolClassT, StudentT } from '../models';
import {environment} from '../../environments/environment'
import { bindCallback, Observable } from 'rxjs';
import { RSA_NO_PADDING } from 'constants';
import { map } from 'rxjs/operators';

const classSecretLength: number = environment.classSecretLength
const teacherPasswordLength: number = environment.teacherPasswordLength
const nHashBits: number = 23
const hashIterations: number  = 3000


export class ClearLocalStudent implements ClearLocalStudentI{
  // id?: number | undefined;
  decryptedName: string;
  // groupBelonging?: number | undefined;
  // selfReported?: boolean | undefined;
  private lastName: string;

  constructor(rawName: string,  public selfReported :boolean, public id?: number,  public groupBelonging?: number ){
    const cleanedStudentName = EncTools.cleanName(rawName) ;
    this.decryptedName = cleanedStudentName;


    this.lastName  = cleanedStudentName.split(" ").slice(-1)[0];
    // console.log(this.lastName)
  }

  static sortStudentsByLastName(students: Array<ClearLocalStudent>): Array<ClearLocalStudent>{
    // this sots INPLACE
    students.sort((a,b)=>a.lastName.localeCompare(b.lastName))
    return students
  }
  static filterAndSort(students: Array<ClearLocalStudent>, groupValue: number): Array<ClearLocalStudent>{
    const flteredStudents = students.filter(s => s.groupBelonging === groupValue)
    const sortedS = ClearLocalStudent.sortStudentsByLastName(flteredStudents);
    console.log("in sorted")
    console.log(sortedS)
    return sortedS
  }
}

// @Injectable({
//   providedIn: 'root'
// })
export class EncTools {
  constructor() { }

  static cleanName(name:string ): string{
    return name.toLowerCase().replace(/\s+/g, ' ').trim().split(' ').map((word)=>{
      return word[0].toUpperCase() + word.substring(1);
    }).join(" ")
  }

  static deriveHash(message: string, salt: string): string{
      const hashBits: string = forge.pkcs5.pbkdf2(message, salt, hashIterations, nHashBits)
      const hashHex: string = forge.util.bytesToHex(hashBits)
      return hashHex
  }

  static generateKeypairAsync(): Observable<forge.pki.rsa.KeyPair>{
    const out:Observable<forge.pki.rsa.KeyPair> = new Observable(subscriber => {
      forge.pki.rsa.generateKeyPair({bits: 2048, workers: 2}, function(err, keypair){
        if (err === null){
          subscriber.next(keypair);
        }
        else {
          subscriber.error(err)
        }
        subscriber.complete()
      })
    });
    return out
  }

  static createRandomString(length: number): string{
    const charset = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ12345678'
    const nChars = charset.length

    var filler: Uint16Array = new Uint16Array(length);
    window.crypto.getRandomValues(filler);
    

    const pw_chars: Array<string> = Array.from(filler).map( i => charset[i%nChars]);
    const password: string = pw_chars.join("")
    return  password
  }

  static createTeacherPassword(): string {
    console.log(">>create teacher password<<")
    return EncTools.createRandomString(teacherPasswordLength)
  }

  static makeClass(schoolName: string, className: string, password: string): Observable<[SchoolClass, ClassTeacher]>{
    // console.log(">>Executing slow make class<<")
    const keysObs: Observable<forge.pki.rsa.KeyPair> = EncTools.generateKeypairAsync();

    const class_secret = EncTools.createRandomString(classSecretLength)

    const finalObs: Observable<[SchoolClass, ClassTeacher]> = keysObs.pipe(map(keys => {
      const schoolClass: SchoolClass = new SchoolClass(schoolName, className, class_secret, keys.publicKey)

      const teacherSecret: string = schoolClass.deriveTeacherSecret(password)

      const classTeacher : ClassTeacher = new ClassTeacher(keys.privateKey, teacherSecret)

      return [schoolClass, classTeacher]
    }));

    return finalObs
  }

  static encrypt(msg: string, publicKey: forge.pki.rsa.PublicKey): string {
    const bytes = forge.util.encodeUtf8(msg)
    const encryptedBytes =  publicKey.encrypt(bytes)
    const encHex: string = forge.util.bytesToHex(encryptedBytes)
    return encHex
  }

  static decrypt(encHex: string, privateKey: forge.pki.rsa.PrivateKey): string {
    const encBytes = forge.util.hexToBytes(encHex)
    const decBytes = privateKey.decrypt(encBytes)
    const message = forge.util.decodeUtf8(decBytes)
    return message
  }

}

export function impl<I>(i: I) { return i; } // weird thing lets you instantiate interfaces

export class SchoolClass{
  constructor(
    public schoolName: string,
    public className: string,
    public classSecret: string,
    public publicKey: forge.pki.rsa.PublicKey,
    public id?: number,
    public surveyStatus?: number
  ) {};

  hashForClass(msg: string): string{
    // hash specific to this class
    return EncTools.deriveHash(msg, this.classSecret)
  }

  deriveTeacherSecret(teacherPassword: string): string{
    return this.hashForClass(teacherPassword)
  }

  name(): string{
    return `${this.schoolName} - ${this.className}`
  }

  get url(): string{
    if (this.id === undefined){
      throw new Error("Cannot get URL of a class that has undefined id")
    }
    return `${this.id}/${this.classSecret}`
  }

  hashStudentName(studentName: string): string {
    return this.hashForClass(studentName)
  }

  encrypt(msg: string): string{
    return EncTools.encrypt(msg, this.publicKey)
  }

  // static makeLocalStudent(decryptedName: string, id?: number, selfReported?: boolean, groupBelonging?:number): ClearLocalStudent{
  //   const cleanedStudentName = EncTools.cleanName(decryptedName)
  //   const localStudent: ClearLocalStudent = ClearLocalStudent(
  //     id: id,
  //     decryptedName: cleanedStudentName,
  //     selfReported: selfReported,
  //     groupBelonging: groupBelonging
  //   )
  //   return localStudent
  // }


  localStudentToTransport(student: ClearLocalStudent): StudentT {
    const nameHash: string = this.hashStudentName(student.decryptedName)
    const encryptedName: string = this.encrypt(student.decryptedName)

    const out: StudentT = impl<StudentT>({
      id: student.id,
      hashedName: nameHash,
      encryptedName: encryptedName,
      groupBelonging: student.groupBelonging,
      selfReported: student.selfReported
    })

    return out
  }

  static fromTransport(transportObject: SchoolClassT): SchoolClass {
    const key: forge.pki.PublicKey = forge.pki.publicKeyFromPem(transportObject.publicKey)

    // if (transportObject.id === undefined){
    //   throw new Error("Can not instantiate a SchoolClass from a Transport that does not contain id")
    // }

    const sClass: SchoolClass = new SchoolClass(
      transportObject.schoolName, 
      transportObject.className,
      transportObject.classSecret,
      key,
      transportObject.id,
      transportObject.surveyStatus
      );

    return sClass
  }

  toTransport(): SchoolClassT{
    const keyString: string = forge.pki.publicKeyToPem(this.publicKey)
    const schoolT: SchoolClassT = impl<SchoolClassT>({
      id: this.id, 
      schoolName: this.schoolName,
      className: this.className,
      classSecret: this.classSecret,
      publicKey: keyString,
      surveyStatus: this.surveyStatus
    })
    return schoolT
  }
}

interface _DecryptedClassTeacherStore{
  id: number,
  teacherSecret: string,
  clearPrivateKey: string
}

export class ClassTeacher{
  constructor(
    private privateKey: forge.pki.rsa.PrivateKey,
    public teacherSecret: string,
    public id?: number
  ){};

  static fromTransport(transportObject: ClassTeacherT, password: string){
    const key: forge.pki.rsa.PrivateKey = forge.pki.decryptRsaPrivateKey(transportObject.encryptedPrivateKey, password)

    return new ClassTeacher(key, transportObject.teacherSecret, transportObject.id )
  }

  decrypt(msg: string): string{
    return EncTools.decrypt(msg, this.privateKey)
  }

  toTransport(password: string): ClassTeacherT{
    const encKey: string = forge.pki.encryptRsaPrivateKey(this.privateKey, password)

    const teachT: ClassTeacherT = impl<ClassTeacherT>({
      id: this.id,
      encryptedPrivateKey: encKey,
      teacherSecret: this.teacherSecret
    })
    return teachT
  }


  clearLocalStudentFromTransport(studentT: StudentT): ClearLocalStudent{
    const studentName = this.decrypt(studentT.encryptedName)
    
    const out: ClearLocalStudent = new ClearLocalStudent(
       studentName,
       studentT.selfReported,
      studentT.id,
       studentT.groupBelonging
    )
    return out
  }


  toJsonString(): string {
     const keyPem: string = forge.pki.privateKeyToPem(this.privateKey)

     if (this.id === undefined){
       throw new Error("Cant convert teacher with missing id to json")
     }

     const toStore: _DecryptedClassTeacherStore = impl<_DecryptedClassTeacherStore>({
       id: this.id,
       teacherSecret: this.teacherSecret,
       clearPrivateKey: keyPem
     })

     return JSON.stringify(toStore)
  }

  static fromJsonString(jsonString: string): ClassTeacher{
    const teacherStore: _DecryptedClassTeacherStore = JSON.parse(jsonString)
    const key = forge.pki.privateKeyFromPem(teacherStore.clearPrivateKey)

    return new ClassTeacher(key, teacherStore.teacherSecret, teacherStore.id)
  }
}


