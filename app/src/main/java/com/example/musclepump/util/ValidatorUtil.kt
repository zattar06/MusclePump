package com.example.musclepump.util

class ValidatorUtil {
    fun nameIsValid(name: String): Boolean {
        val nameIsNotNull = name.isNotEmpty()
        val nameSizeIsValid = name.split(" ").size >= 2
        return nameIsNotNull && nameSizeIsValid
    }

    fun emailIsValid(email: String): Boolean {
        val emailIsNotNull = email.isNotEmpty()
        val emailRegex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,})+\$")
        val emailIsValid = emailRegex.matches(email)
        return emailIsNotNull && emailIsValid
    }

    fun pwdIsValidForLogin(pwd: String): Boolean {
        val pwdIsNotNull = pwd.isNotEmpty()
        val pwdSizeIsValid = pwd.length > 6
        return pwdIsNotNull && pwdSizeIsValid
    }

    fun pwdIsValidForRegister(pwd: String, pwdConfirm: String): Boolean {
        val pwdIsNotNull = pwd.isNotEmpty()
        val pwdSizeIsValid = pwd.length > 6
        val pwdIsValid = pwd == pwdConfirm
        return pwdIsNotNull && pwdSizeIsValid && pwdIsValid
    }
}