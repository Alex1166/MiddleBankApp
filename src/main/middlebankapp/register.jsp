<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <title>Registration</title>
</head>

<body>

<form onreset="document.getElementById('warning').hidden = true;" onsubmit="return checkPassword();" action="api/v1/register" method="POST">
    <p>
        <label>
            <input type="text" id="username" name="username" placeholder="Enter login" required>
        </label>
    </p>
    <p>
        <label>
            <input type="password" id="password" name="password" placeholder="Enter password" required>
        </label>
    </p>
    <p>
        <label>
            <input type="password" id="passwordConfirmation" name="password_confirmation" placeholder="Confirm password" required>
        </label>
    </p>
    <p>
        <b hidden id="warning">Неверный пароль</b>
    </p>

    <input type="submit" value="Log in">
    <input type="reset" value="Clear">
</form>

</body>
<script>
    function checkPassword(){
        document.getElementById('warning').hidden = true;
        if (document.getElementById('password').value == document.getElementById('passwordConfirmation').value) {
            return true;
        } else {
            document.getElementById('warning').hidden = false;
            return false;
        }
    }
</script>

</html>