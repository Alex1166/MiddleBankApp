<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<script src="js/scripts.js"></script>
<head>
    <title>Bank profile</title>
</head>
<body>
    <h2>Choose an operation</h2>
    <label for="pet-select">Choose your account:</label>
    <div id="api-accounts">
    </div>

    <form id="transferForm" method="POST" action='api/v1/transfer'>
        <input type="hidden" id="transferAccountId" name="account" value="" />
        <p>
            <label>
                <input type="text" id="moneyToTransfer" name="money" placeholder="Enter money to transfer" required>
            </label>
        </p>
        <p>
            <label>
                <input type="text" id="recipientLogin" name="recipient" placeholder="Enter recipient login" required>
            </label>
        </p>
        <input type="submit" value="Transfer">
    </form>

    </br>

    <form id="depositForm" method="POST" action='api/v1/deposit'>
        <input type="hidden" id="depositAccountId" name="account" value="" />
        <input type="hidden" id="senderLogin" name="sender" value="" />
        <p>
            <label>
                <input type="text" id="moneyToDeposit" name="money" placeholder="Enter money to deposit" required>
            </label>
        </p>
        <input type="submit" value="Deposit">
    </form>

    </br>

    <form id="withdrawForm" method="POST" action='api/v1/withdraw'>
        <input type="hidden" id="withdrawAccountId" name="account" value="" />
        <p>
            <label>
                <input type="text" id="moneyToWithdraw" name="money" placeholder="Enter money to withdraw" required>
            </label>
        </p>
        <input type="submit" value="Withdraw">
    </form>
    </br>
    <p>
        <b hidden id="warning">Неверно введены данные</b>
    </p>

    </br>
    <a href="profile">back to profile</a>
</body>
<script>
    function loadAccounts() {
        fetch('api/v1/accounts')
            .then(response => response.json())
            .then(data => {
                let html = '<select name="accounts" id="account-select" onchange="setAccount(this)">';
                data['accounts'].forEach(account => {
                    setAccount(`\${account.userId}`);
                    html += `<option value=\${account.id}>\${account.title}: \${account.balance}</option>`;
                });
                html += '</select>';
                document.getElementById('api-accounts').innerHTML = html;
            })
            .catch(error => {
                console.error(error);
                document.getElementById('api-accounts').innerText = 'Error: ' + error;
            });
    }
    function setAccount(value) {
        console.log(value);
        document.getElementById('transferAccountId').value = value;
        document.getElementById('depositAccountId').value = value;
        document.getElementById('withdrawAccountId').value = value;
    }
    loadAccounts();

    document.getElementById("transferForm").addEventListener("submit", function(event) { submitProcess(event); });
    document.getElementById("depositForm").addEventListener("submit", function(event) { submitProcess(event); });
    document.getElementById("withdrawForm").addEventListener("submit", function(event) { submitProcess(event); });
</script>
</html>