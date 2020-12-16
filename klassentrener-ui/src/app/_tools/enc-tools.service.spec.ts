import { TestBed } from '@angular/core/testing';

import { ClassTeacher, EncTools, impl, SchoolClass } from './enc-tools.service';
import * as forge from 'node-forge';
import { ClearLocalStudent } from '../models';


describe('EncTools', () => {
  let service: EncTools;
  const passwordG = "testPassword2939"
  const [schoolClass, classTeacher] = EncTools.makeClass(
      "test school", "test class", passwordG
  )

  it('should encrypt decrypt', () =>{
    const keys = forge.pki.rsa.generateKeyPair()

    const test = "hallo this is a test message"

    const encrypted = EncTools.encrypt(test, keys.publicKey)

    console.log(`encrypted: ${encrypted}`)

    const recon = EncTools.decrypt(encrypted, keys.privateKey)

    expect(test).toEqual(recon)
  })

  it('should serialise desirialise teacher and class', () =>{

    classTeacher.id = 23

    const schoolT = schoolClass.toTransport()
    const teacherT = classTeacher.toTransport(passwordG)


    const recoveredClass = SchoolClass.fromTransport(schoolT)
    const recoveredTeach = ClassTeacher.fromTransport(teacherT, passwordG)

    console.log(schoolClass)
    console.log(recoveredClass)
    expect(JSON.stringify(recoveredClass)).toEqual(JSON.stringify(schoolClass))

    const testMessage = "this is some test message ladida"
    const encMsg = schoolClass.encrypt(testMessage)
    
    // check both classes behave the same
    expect(schoolClass.deriveTeacherSecret(passwordG)).toEqual(recoveredClass.deriveTeacherSecret(passwordG))

    console.log(classTeacher)
    console.log(recoveredTeach)
    expect(recoveredTeach.toJsonString()).toEqual(classTeacher.toJsonString())    


    const teacherString = classTeacher.toJsonString()
    const teacherFromString = ClassTeacher.fromJsonString(teacherString)

    // all versions of recovered teachers have to be able to decrypt
    expect(classTeacher.decrypt(encMsg)).toEqual(testMessage)
    expect(recoveredTeach.decrypt(encMsg)).toEqual(testMessage)
    expect(teacherFromString.decrypt(encMsg)).toEqual(testMessage)

    //TODO make sure the student ids are transmitted in a sensible way
    // student -> enc -> set id -> dec -> check-same -> end -> check id same
  })

  it('should serialize desirialise students', () => {
    const name = "Lé Hans Müller"

    const testStudent = impl<ClearLocalStudent>({
      decryptedName: name,
    }) 

    var studentTransport = schoolClass.localStudentToTransport(testStudent)

    expect(studentTransport.hash).toBeTruthy()
    expect(studentTransport.encryptedName === name).toBeFalse()

    // set ids and stuff

    studentTransport.id = 42
    studentTransport.selfReported = true
    studentTransport.groupBelonging = 3

    const localTwo: ClearLocalStudent = classTeacher.clearLocalStudentFromTransport(studentTransport)

    expect(localTwo.decryptedName).toEqual(name)
    expect(localTwo.groupBelonging).toEqual(studentTransport.groupBelonging)

    const transportTwo = schoolClass.localStudentToTransport(localTwo)

    // check the two transports are equal
    expect(transportTwo.id).toEqual(studentTransport.id)
    expect(transportTwo.hash).toEqual(studentTransport.hash)
    expect(transportTwo.groupBelonging).toEqual(studentTransport.groupBelonging)
    expect(transportTwo.selfReported).toEqual(studentTransport.selfReported)
    expect(transportTwo.encryptedName).not.toEqual(studentTransport.encryptedName)
    console.log(studentTransport)
  })
});
