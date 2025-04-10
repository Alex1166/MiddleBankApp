<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<script src="js/scripts.js"></script>
<head>
    <meta charset="UTF-8">
    <title>Login</title>
</head>
<body>
<form id="loginForm" action="api/v1/login" method="POST">
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
    <input type="submit" value="Log in">
    <input type="reset" value="Clear">
</form>
</br>
<p>
    <b hidden id="warning">Неверно введены данные</b>
</p>
<a href="register">register page</a>
</body>
<script>
    document.getElementById("loginForm").addEventListener("submit", function(event) { submitProcess(event); });
</script>
</html>