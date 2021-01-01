import { SelfReportedStudent } from "./teacher-clean-up.models";



fdescribe("teacher clean up models", ()=>{

    it("test 1", () =>{
        console.log("test 1")

        const teachStudent = new SelfReportedStudent(23, "Max Müller");

        console.log(teachStudent.name)
    })


});