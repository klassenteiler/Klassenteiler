import { stringify } from "@angular/compiler/src/util"
import { type } from "os"
import { impl } from "src/app/_tools/enc-tools.service"

interface StudentInEditStore{
    _currentName: string,
    _teacherAdded: boolean,
    _id: number | null,
    _deleted: boolean,
    _origName: string | null
}


export class SelfReportedInEdit{
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
    static fromStoreI(store: StudentInEditStore): SelfReportedInEdit {
        const s: SelfReportedInEdit = new SelfReportedInEdit(
            store._currentName,
            store._teacherAdded,
            store._id,
            store._deleted,
            store._origName
        )
        return s
    }

    static students2JSON(students: Array<SelfReportedInEdit>): string{
        const studentsI : Array<StudentInEditStore> = students.map(s => s.toStoreI())
        const jsonS: string = JSON.stringify(studentsI)
        return jsonS
    }
    static json2Students(jsonS: string): Array<SelfReportedInEdit> {
        const studentsI: Array<StudentInEditStore> = JSON.parse(jsonS)
        const students: Array<SelfReportedInEdit> = studentsI.map(sI => SelfReportedInEdit.fromStoreI(sI))
        return students
    }

    static copyStudents(sdtns: SelfReportedInEdit[]): SelfReportedInEdit[]{
        const s: string = SelfReportedInEdit.students2JSON(sdtns)
        return SelfReportedInEdit.json2Students(s)
    }

    static makeSelfReported(name: string, id: number){
        return new SelfReportedInEdit(
            name,
            false,
            id,
            false,
            name
        )
    }

    static makeTeacherAdded(name: string){
        return new SelfReportedInEdit(
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


    delete(): boolean{
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

interface FriendReported2MatchStore{
    _id: number;
    _name: string;
    _matchedSelfReportedName: string | null ;
    _shouldBeDeleted: boolean;
    // _wasMatched: boolean;
}

export class FriendReported2Match{
    constructor(
        private _id: number, 
        private _name: string, 
        private _matchedSelfReportedName: string | null,
        private _shouldBeDeleted: boolean, // delete was assigned
        // private _wasMatched: boolean, // was matched to an actual name
        ){
    }

    static makeFriendReported2Match(nname: string, id: number){
        return new FriendReported2Match(
            id, 
            nname, 
            null,
            false,
            // false
            )
    }

    toStoreI(): FriendReported2MatchStore{
        const sto: FriendReported2MatchStore = impl<FriendReported2MatchStore>({
            _id: this._id,
            _name: this._name,
            _matchedSelfReportedName: this._matchedSelfReportedName,
            _shouldBeDeleted: this._shouldBeDeleted,
            // _wasMatched: this._wasMatched
        })
        return sto
    }

    static fromStoreI(sto: FriendReported2MatchStore): FriendReported2Match{
        const instance: FriendReported2Match = new FriendReported2Match(
            sto._id, sto._name, sto._matchedSelfReportedName, sto._shouldBeDeleted
        )
        return instance
    } 

    static array2JSON(students: Array<FriendReported2Match>): string{
        const studentsI : Array<FriendReported2MatchStore> = students.map(s => s.toStoreI())
        const jsonS: string = JSON.stringify(studentsI)
        return jsonS
    }
    static json2array(jsonS: string): Array<FriendReported2Match> {
        const studentsI: Array<FriendReported2MatchStore> = JSON.parse(jsonS)
        const students: Array<FriendReported2Match> = studentsI.map(sI => FriendReported2Match.fromStoreI(sI))
        return students
    }

    get name(): string {return this._name}
    get id(): number {return this._id}

    get matchedSelfReportedName(): string {
        if(!this.wasMatched){ throw new Error("cant get the matched name because this was not matched")}
        return this._matchedSelfReportedName!}

    get wasMatched(): boolean {
        // return (typeof this._matchedSelfReportedName === "string"); 
        return this._matchedSelfReportedName !== null;
    }

    get shouldBeDeleted(): boolean{ return this._shouldBeDeleted}

    set matchedSelfReportedName(n: string){
        this._matchedSelfReportedName = n
        this._shouldBeDeleted = false;
    }

    get hasBeenAssigned(): boolean{
        return (this.wasMatched || this.shouldBeDeleted)
    }

    reset(){
        this._matchedSelfReportedName = null;
        this._shouldBeDeleted = false;
    }
    delete(){
        this._matchedSelfReportedName = null;
        this._shouldBeDeleted = true;
    }

}