import { TestBed } from '@angular/core/testing';
import { assert } from 'console';
import { of } from 'rxjs';
import { first } from 'rxjs/operators';
import { TeacherService } from 'src/app/_services/teacher.service';
import { ClassTeacher, ClearLocalStudent, EncTools, SchoolClass } from 'src/app/_tools/enc-tools.service';

import { MergeService } from './merge.service';
import { StudentInEdit } from './teacher-clean-up.models';

class MockTeacherService{

  static makeBackendStudents(){
    const studentsFromBackend: Array<ClearLocalStudent> = [
      // TODO These are the wrong type of studens. the get teacher service should return ClearLocalStudent instances
      new ClearLocalStudent( "Max Müller", true, 23),
      new ClearLocalStudent( "Peter Was", true, 24),
      new ClearLocalStudent( "Maria Dieter", true, 25),
      new ClearLocalStudent("Last One", true, 37)
    ]
  return studentsFromBackend
  }
}


describe('MergeService', () => {
  let service: MergeService;
  var teacherServiceSpy = jasmine.createSpyObj('TeacherService', ['getSelfReported'])



let schoolClass: SchoolClass|null =null;
let classTeacher: ClassTeacher|null =null;
const passwordG:string = "testPassword"

beforeEach(async () => {

  const promise : Promise<[SchoolClass, ClassTeacher]> = EncTools.makeClass(
    "test school", "test class", passwordG
  ).toPromise();

  const [schoolClassTmp, classTeacherTmp]  = await promise;
  schoolClassTmp.id=42;
  classTeacherTmp.id=42;
  schoolClass = schoolClassTmp;
  classTeacher = classTeacherTmp;
});

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        {provide: TeacherService, useValue:  teacherServiceSpy}
      ]
    });
    service = TestBed.inject(MergeService);
  });

  afterEach(()=>{
    localStorage.clear();
  })

  it('should be created', () => {
    expect(service).toBeTruthy();
  });


  it('should recover partial edit state if and only if backend state has not changed', async () => {
    teacherServiceSpy.getSelfReported.and.returnValue(of(MockTeacherService.makeBackendStudents()));

    const [mergeHash, firstState] = await service.getMergeState(schoolClass!, classTeacher!).toPromise()

    const firstClasslist: StudentInEdit[] = firstState;

    expect(firstClasslist.map(s=>s.name)).toEqual(MockTeacherService.makeBackendStudents().map(s=>s.decryptedName)) // start with state same as students
    console.log(mergeHash)

    var secondClasslist  = StudentInEdit.copyStudents(firstClasslist)
    // Do some changes

    secondClasslist[0].delete()
    secondClasslist[1].name = "changed name"
    // save second state, which was derived with the same hash
    service.saveState2local(schoolClass!, mergeHash, secondClasslist)


    // now we assume the teacher reloaded, so we get the state again
    // the backend still returns the same original class list
    const [thirdHash, thirdState] = await service.getMergeState(schoolClass!, classTeacher!).toPromise()
    const thirdClassList: StudentInEdit[] = thirdState

    expect(thirdHash).toEqual(mergeHash);

    // the edited state from before should be reloaded
    expect(thirdClassList).toEqual(secondClasslist)
    expect(thirdClassList).not.toEqual(firstClasslist)


    // now we pretend the backend service changed
    const newBackendStudentList : ClearLocalStudent[] = MockTeacherService.makeBackendStudents().concat([ new ClearLocalStudent("Extra Student", true, 75)]);
    teacherServiceSpy.getSelfReported.and.returnValue(of(newBackendStudentList));
    // now the teacher again reloads

    const [fourthHash, fourthState] = await service.getMergeState(schoolClass!, classTeacher!).toPromise();
    expect(fourthHash).not.toEqual(mergeHash)
    expect(fourthState).not.toEqual(thirdClassList); // now the edit state was not reloaded

    expect(fourthState.map(s=> s.name)).toEqual(newBackendStudentList.map(s=>s.decryptedName))

    expect(fourthState[0].deleted).toBeFalse()// all edits should be lost
    expect(fourthState[1].changed).toBeFalse()

    // now assume that the previous state still exists in a different browser window were it is trying to be saved. 

    console.log("about to try illegal save")

    spyOn(service, 'windowReload').and.callFake(function(){});
    service.saveState2local(schoolClass!, thirdHash, thirdClassList)
    expect(service.windowReload).toHaveBeenCalled()
    console.log('done')

    // In the reload this happends
    const [fifthHash, fifthState] = await service.getMergeState(schoolClass!, classTeacher!).toPromise();
    expect(fourthState.map(s=> s.name)).toEqual(newBackendStudentList.map(s=>s.decryptedName))
    // which should be the initial state with the 'extra student'

  });
});

// - can't submit merge result derived from old state hash