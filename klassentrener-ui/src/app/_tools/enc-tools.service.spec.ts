import { TestBed } from '@angular/core/testing';

import { ClassTeacher, EncTools, SchoolClass } from './enc-tools.service';
import * as forge from 'node-forge';


describe('EncTools', () => {
  let service: EncTools;
  const passwordG = "testPassword2939"

  it('should encrypt decrypt', () =>{
    const keys = forge.pki.rsa.generateKeyPair()

    const test = "hallo this is a test message"

    const encrypted = EncTools.encrypt(test, keys.publicKey)

    console.log(`encrypted: ${encrypted}`)

    const recon = EncTools.decrypt(encrypted, keys.privateKey)

    expect(test).toEqual(recon)
  })

  it('should serialise desirialise objects', () =>{
    var [schoolClass, classTeacher] = EncTools.makeClass(
      "test school", "test class", passwordG
    )

    classTeacher.id = 23

    const schoolT = schoolClass.toTransport()
    const teacherT = classTeacher.toTransport(passwordG)


    const recoveredClass = SchoolClass.fromTransport(schoolT)
    const recoveredTeach = ClassTeacher.fromTransport(teacherT, passwordG)

    console.log(schoolClass)
    console.log(recoveredClass)
    expect(JSON.stringify(recoveredClass)).toEqual(JSON.stringify(schoolClass))
    
    // check both classes behave the same
    expect(schoolClass.deriveTeacherSecret(passwordG)).toEqual(recoveredClass.deriveTeacherSecret(passwordG))

    console.log(classTeacher)
    console.log(recoveredTeach)
    expect(recoveredTeach.toJsonString()).toEqual(classTeacher.toJsonString())    


    const teacherString = classTeacher.toJsonString()
    const teacherFromString = ClassTeacher.fromJsonString(teacherString)

    // check that all classes give same hashes


  })
});
