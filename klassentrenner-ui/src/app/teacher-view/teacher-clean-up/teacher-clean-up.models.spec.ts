import { FriendReported2Match, SelfReportedInEdit } from "./teacher-clean-up.models";



describe("teacher-clean-up models", ()=>{

    it("should serialize and desirialise the class list elements", () =>{
        console.log("test 1")

        const students: Array<SelfReportedInEdit> = [
            SelfReportedInEdit.makeSelfReported( "Max Müller", 23),
            SelfReportedInEdit.makeSelfReported( "Peter Was", 24),
            SelfReportedInEdit.makeSelfReported( "Maria Dieter", 25),
            SelfReportedInEdit.makeTeacherAdded("Teacher Added")
        ]

        students[2].name = "changed Name"
        students[0].delete()

        const jsonS:string = SelfReportedInEdit.students2JSON(students);
        const recoveredStudents: Array<SelfReportedInEdit> = SelfReportedInEdit.json2Students(jsonS);

        expect(students).toEqual(recoveredStudents);
        expect(recoveredStudents[0].deleted).toBeTrue();
        expect(recoveredStudents[2].changed).toBeTrue();
    })

    it("should copy a list of students", () =>{
        const students: Array<SelfReportedInEdit> = [
            SelfReportedInEdit.makeSelfReported( "Max Müller", 23),
            SelfReportedInEdit.makeSelfReported( "Peter Was", 24),
            SelfReportedInEdit.makeSelfReported( "Maria Dieter", 25),
            SelfReportedInEdit.makeTeacherAdded("Teacher Added")
        ]

        const studentsCopy = SelfReportedInEdit.copyStudents(students);

        expect(studentsCopy).toEqual(students)

        // no modify the copy
        studentsCopy[2].delete()

        expect(students[2].deleted).toBeFalse();
        expect(studentsCopy[2].deleted).toBeTrue();
    })

    it("should serialise a list of friend reported students", () =>{
        const students: Array<FriendReported2Match> = [
            FriendReported2Match.makeFriendReported2Match( "Max Müller", 23),
            FriendReported2Match.makeFriendReported2Match( "Peter Nur", 24),
            FriendReported2Match.makeFriendReported2Match( "Maria Dieter", 25),
            FriendReported2Match.makeFriendReported2Match("Teacher Added", 26)
        ]

        students[1].matchedSelfReportedName = "Peter Nuhr"

        const serialised: string = FriendReported2Match.array2JSON(students);

        const recon: FriendReported2Match[] = FriendReported2Match.json2array(serialised)

        expect(students).toEqual(recon)
        expect(recon[1].matchedSelfReportedName).toBeTruthy()
        expect(recon[0].matchedSelfReportedName).toBeFalsy()
    })
});