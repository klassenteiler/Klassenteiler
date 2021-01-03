import { StudentInEdit } from "./teacher-clean-up.models";



describe("teacher-clean-up models", ()=>{

    it("should serialize and desirialise the class list elements", () =>{
        console.log("test 1")

        const students: Array<StudentInEdit> = [
            StudentInEdit.makeSelfReported( "Max Müller", 23),
            StudentInEdit.makeSelfReported( "Peter Was", 24),
            StudentInEdit.makeSelfReported( "Maria Dieter", 25),
            StudentInEdit.makeTeacherAdded("Teacher Added")
        ]

        students[2].name = "changed Name"
        students[0].delete()

        const jsonS:string = StudentInEdit.students2JSON(students);
        const recoveredStudents: Array<StudentInEdit> = StudentInEdit.json2Students(jsonS);

        expect(students).toEqual(recoveredStudents);
        expect(recoveredStudents[0].deleted).toBeTrue();
        expect(recoveredStudents[2].changed).toBeTrue();
    })

    it("should copy a list of students", () =>{
        const students: Array<StudentInEdit> = [
            StudentInEdit.makeSelfReported( "Max Müller", 23),
            StudentInEdit.makeSelfReported( "Peter Was", 24),
            StudentInEdit.makeSelfReported( "Maria Dieter", 25),
            StudentInEdit.makeTeacherAdded("Teacher Added")
        ]

        const studentsCopy = StudentInEdit.copyStudents(students);

        expect(studentsCopy).toEqual(students)

        // no modify the copy
        studentsCopy[2].delete()

        expect(students[2].deleted).toBeFalse();
        expect(studentsCopy[2].deleted).toBeTrue();

    })

});