

export abstract class Student4Edit{
    protected _deleted: boolean = false;

    constructor(protected _name: string){}

    get name(): string { return this._name}
    set name(nname:string) {this._name = nname;}

    get deleted(): boolean { return this._deleted}

    abstract get teacherAdded(): boolean;

    // i.e. if there is an original state that could be reverted to, either deleted/name changed
    get recoverable(): boolean{return (this.changed || this.deleted)}; 

    abstract recover(): void;

    // returns true if this object should be destroyed
    abstract delete(): boolean;

    // i.e. should 
    abstract get changed(): boolean;

    // the name that it should be recovered to
    get recoverName():string {return ""};

}

export class SelfReportedStudent extends Student4Edit{
    private _origName: string;

    get teacherAdded()  {return false}

    constructor(public id: number, nname: string){
        super(nname);
        this._origName = nname;
    }

    get changed(): boolean {
        return this.name !== this._origName
    }

    recover(){
        super.name = this._origName
        super._deleted = false;
    }

    get recoverName(): string {
        return this._origName
    }

    delete(): boolean{
        super._deleted = true;
        return false;
    }
}

export class TeacherAddedStudent extends Student4Edit{
    get teacherAdded()  {return true}

    constructor(nname: string){
        super(nname);
    }

    get changed(): boolean {return false}

    delete(): boolean{
        super._deleted = true;
        return true;
    }

    recover() {
        throw new Error("Teacher added student cant be reset")
    }
}
