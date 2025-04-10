<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<script src="js/scripts.js"></script>
<head>
    <title>Bank profile</title>
</head>
<body>
    <h2>Welcome, <span id="_username_"></span></h2>
    <h2>Account List</h2>
    <table id="_accounts_" border="1">
        <tr>
            <th>ID</th>
            <th>Title</th>
            <th>Balance</th>
            <th>Set default</th>
        </tr>
    </table>
    </br>
    <p>
        <b hidden id="warning">Неверно введены данные</b>
    </p>
    </br>
    <form id="createForm" method="POST" action="api/v1/accounts">
        <input type="text" id="title" name="title" placeholder="Enter title">
        <button type="submit">Create new account</button>
    </form>
    </br>
    <a href="transfer">Transfer money</a>
    <a href="api/v1/logout">login page</a>
</body>
</br>
<p>
    <b hidden id="warning">Неверно введены данные</b>
</p>
<script>
    document.addEventListener("DOMContentLoaded", async function(event) {
        const user = await getJson("api/v1/login");
        console.log(user);
        document.getElementById("_username_").innerText = user.login;

        const accounts = await getJson("api/v1/accounts");
        console.log(accounts);
        var html;
        for (var account of accounts['accounts']) {
            console.log(account);

            var tr = document.createElement('tr');

            var td = document.createElement('td');
            td.textContent = account.id;
            tr.appendChild(td);

            td = document.createElement('td');
            td.textContent = account.title;
            tr.appendChild(td);

            td = document.createElement('td');
            td.textContent = account.balance;
            tr.appendChild(td);

            td = document.createElement('td');
            if (account.isDefault) {
                td.textContent = "✅";
            } else {
                var form = document.createElement('form');
                form.id = 'setDefaultForm' + account.id;
                form.method = "PATCH";
                form.action = "api/v1/accounts/" + account.id;

                var input = document.createElement('input');
                input.type = "hidden";
                input.name = "default";
                input.value = "True";
                form.appendChild(input);

                var button = document.createElement('button');
                button.type = "submit";
                button.textContent = "set";
                form.appendChild(button);

                td.appendChild(form);
            }
            tr.appendChild(td);

            document.getElementById("_accounts_").appendChild(tr);

            if (!account.isDefault) {
                document.getElementById("setDefaultForm" + account.id).addEventListener("submit", function(event) { submitProcess(event); });
            }
        }
    })
</script>
</html>