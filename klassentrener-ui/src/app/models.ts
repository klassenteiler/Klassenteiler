export interface SchoolClassT{
    id?: number,
    className: string,
    schoolName: string,
    classSecret: string,
    publicKey: string,
    status?: string
}

export interface ClassTeacherT{
    id?: number,
    encryptedPrivateKey: string,
    teacherSecret: string
}

export interface StudentT{
    id?: number,
    hash?: string,
    encryptedName: string,
    groupBelonging?: number,
    selfReported?: boolean
}

export interface ClearLocalStudent{
    id?: number,
    decryptedName: string,
    groupBelonging?: number,
    selfReported?: boolean
}