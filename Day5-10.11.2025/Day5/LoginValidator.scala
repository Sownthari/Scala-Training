def validateLogin(userName: String, password: String): Either[String, String] = {
    if userName.isEmpty then Left("Username is missing")
    else if password.isEmpty then Left("Password is missing")
    else Right("Login successful")
}

@main def runValidation(): Unit =
  println(validateLogin("", "123"))       // Left(Username missing)
  println(validateLogin("user", ""))      // Left(Password missing)
  println(validateLogin("user", "123"))   // Right(Login successful)