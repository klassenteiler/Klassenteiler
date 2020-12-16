import { Injectable } from '@angular/core';
import * as forge from 'node-forge';
import { ClassTeacherT, ClearLocalStudent, SchoolClassT, StudentT } from '../models';
import {environment} from '../../environments/environment'

const classSecretLength: number = environment.classSecretLength
const teacherPasswordLength: number = environment.teacherPasswordLength
const nHashBits: number = 23
const hashIterations: number  = 3000

// @Injectable({
//   providedIn: 'root'
// })
export class EncTools {
  constructor() { }

  static deriveHash(message: string, salt: string): string{
      const hashBits: string = forge.pkcs5.pbkdf2(message, salt, hashIterations, nHashBits)
      const hashHex: string = forge.util.bytesToHex(hashBits)
      return hashHex
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

  static makeClass(schoolName: string, className: string, password: string): [SchoolClass, ClassTeacher]{
    const keys = forge.pki.rsa.generateKeyPair();

    const class_secret = EncTools.createRandomString(classSecretLength)

    const schoolClass: SchoolClass = new SchoolClass(schoolName, className, class_secret, keys.publicKey)

    const teacherSecret: string = schoolClass.deriveTeacherSecret(password)

    const classTeacher : ClassTeacher = new ClassTeacher(keys.privateKey, teacherSecret)

    return [schoolClass, classTeacher]
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
    public status?: string
  ) {};

  hashForClass(msg: string): string{
    // hash specific to this class
    return EncTools.deriveHash(msg, this.classSecret)
  }

  deriveTeacherSecret(teacherPassword: string): string{
    return this.hashForClass(teacherPassword)
  }

  hashStudentName(studentName: string): string {
    return this.hashForClass(studentName)
  }

  encrypt(msg: string): string{
    return EncTools.encrypt(msg, this.publicKey)
  }


  localStudentToTransport(student: ClearLocalStudent): StudentT {
    const nameHash: string = this.hashStudentName(student.decryptedName)
    const encryptedName: string = this.encrypt(student.decryptedName)

    const out: StudentT = impl<StudentT>({
      id: student.id,
      hash: nameHash,
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
      transportObject.status
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
      status: this.status
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
    
    const out: ClearLocalStudent = impl<ClearLocalStudent>({
      id: studentT.id,
      decryptedName: studentName,
      groupBelonging: studentT.groupBelonging,
      selfReported: studentT.selfReported
    })
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

