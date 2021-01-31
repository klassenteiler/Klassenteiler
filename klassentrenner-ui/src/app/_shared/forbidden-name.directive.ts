import { AbstractControl, ValidatorFn } from "@angular/forms";

/** A hero's name can't match the given regular expression */
export class ForbiddenNameValidator{
  // constructor(public listOfNames: string[]){}
  currentClassListNames: string[] = [];
  // TODO very hacky. construct this outside in a komponent, use validator.func.bind(this) in the component to bind the component to this
  // rely on the component having a currentClassListNames field. very very hacky!

    func (control: AbstractControl): {[key: string]: any} | null{
      console.log(this.currentClassListNames)
      const foundId: number = this.currentClassListNames.indexOf(control.value)
      const forbidden = foundId !== -1;
      return forbidden ? {forbiddenName: {arrayPosition: foundId}} : null;
    };
}