import { AbstractControl, ValidatorFn } from "@angular/forms";


/** A hero's name can't match the given regular expression */
export class ForbiddenNameValidator{
  constructor(public listOfNames: string[]){}

    func (control: AbstractControl): {[key: string]: any} | null{
      console.log(this.listOfNames)
      const foundId: number = this.listOfNames.indexOf(control.value)
      const forbidden = foundId !== -1;
      return forbidden ? {forbiddenName: {arrayPosition: foundId}} : null;
    };
}