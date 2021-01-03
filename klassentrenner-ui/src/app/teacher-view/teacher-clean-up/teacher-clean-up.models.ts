import { impl } from "src/app/_tools/enc-tools.service"

interface StudentInEditStore{
    _currentName: string,
    _teacherAdded: boolean,
    _id: number | null,
    _deleted: boolean,
    _origName: string | null
}


export class StudentInEdit{
    constructor(
        private _currentName: string,
        private _teacherAdded: boolean,
        private _id: number | null,
        private _deleted: boolean,
        private _origName: string | null
    ){}

    toStoreI(): StudentInEditStore{
        const toStore: StudentInEditStore = impl<StudentInEditStore>({
            _currentName: this._currentName,
            _teacherAdded: this._teacherAdded,
            _id: this._id,
            _deleted: this._deleted,
            _origName: this._origName
        })
        return toStore

    }
    static fromStoreI(store: StudentInEditStore): StudentInEdit {
        const s: StudentInEdit = new StudentInEdit(
            store._currentName,
            store._teacherAdded,
            store._id,
            store._deleted,
            store._origName
        )
        return s
    }

    static students2JSON(students: Array<StudentInEdit>): string{
        const studentsI : Array<StudentInEditStore> = students.map(s => s.toStoreI())
        const jsonS: string = JSON.stringify(studentsI)
        return jsonS
    }
    static json2Students(jsonS: string): Array<StudentInEdit> {
        const studentsI: Array<StudentInEditStore> = JSON.parse(jsonS)
        const students: Array<StudentInEdit> = studentsI.map(sI => StudentInEdit.fromStoreI(sI))
        return students
    }

    static copyStudents(sdtns: StudentInEdit[]): StudentInEdit[]{
        const s: string = StudentInEdit.students2JSON(sdtns)
        return StudentInEdit.json2Students(s)
    }

    static makeSelfReported(name: string, id: number){
        return new StudentInEdit(
            name,
            false,
            id,
            false,
            name
        )
    }

    static makeTeacherAdded(name: string){
        return new StudentInEdit(
            name,
            true,
            null,
            false,
            null
        )
    }

    get name(): string { return this._currentName}
    get deleted(): boolean { return this._deleted}
    get teacherAdded(): boolean { return this._teacherAdded}

    get changed(): boolean {
        if (this.teacherAdded){return false;}
        else {
            return (this._origName !== this._currentName)
        }
    }

    get recoverable(): boolean{
        if (this.teacherAdded){ return false;}
        else{
            return (this.deleted || this.changed)
        }
    }

    get recoverName(): string{
        if(this.teacherAdded){ return ""}
        else{
            return this._origName!
        }
    }

    set name(nname:string){
        if (this.deleted){ throw new Error("cant change name on a deleted student")}

        this._currentName = nname
    }


    delete():boolean{
        this._deleted = true;

        if(this.teacherAdded){
            return true; // this should be destroyed
        }
        else{
            if(this._origName === null){ throw new Error("_origName was null on a self reported student.")}
            this._currentName = this._origName;
            return false;
        }
    }

    recover(){
        if(this.teacherAdded){ throw new Error("A teacher added student can't be recovered")}
        else{
            this._deleted = false;
            if(this._origName === null){throw new Error("_origName was null on self reported student")}
            this._currentName = this._origName!
        }
    }
}

