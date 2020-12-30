export interface SchoolClassT{
    id?: number,
    className: string,
    schoolName: string,
    classSecret: string,
    publicKey: string,
    surveyStatus?: string
}

export interface ClassTeacherT{
    id?: number,
    encryptedPrivateKey: string,
    teacherSecret: string
}

export interface StudentT{
    id?: number,
    hash: string,
    encryptedName: string,
    groupBelonging?: number,
    selfReported: boolean
}

export interface ClearLocalStudentI{
    id?: number,
    decryptedName: string,
    groupBelonging?: number,
    selfReported: boolean
}

export interface NumericValueT{
    value: number
}

export interface StringMessageT{
    message: string
}

export const SchoolClassSurveyStatus = {
    open: "open",
    closed: "closed",
    calculating: "calculating",
    done: "done"

}