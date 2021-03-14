import { Directive } from "@angular/core";
import { AbstractControl, NG_VALIDATORS, ValidationErrors, Validator, ValidatorFn } from "@angular/forms";
import { EncTools } from "../_tools/enc-tools.service";


        export const uniqueNamesValidatorFunc: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
            // console.log(this.currentClassListNames) 
            const ownName: string = control.get('ownName')!.value           
            const friendsNames: string[] = control.get('friendsNames')!.value

            console.log(ownName)
            console.log(friendsNames)

            const me: string = EncTools.cleanName(ownName)
            const other: string[] = friendsNames.map(EncTools.cleanName);
        
            if( other.indexOf(me) != -1){
                return {nonUniqueName: {type: 'own', name: me}}
            }
            for(let i = 0; i < other.length;i++) {
              // compare the first and last index of an element
              if(other.indexOf(other[i]) !== other.lastIndexOf(other[i])){
                return {nonUniqueName: {type: 'friend', name: other[i]}}
              }
            }

            return null
        };