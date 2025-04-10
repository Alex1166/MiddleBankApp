function submitProcess(event) {
    const form = event.currentTarget;

    event.preventDefault();
    const formData = new FormData(form);
    const jsonObject = Object.fromEntries(formData.entries());
    const jsonString = JSON.stringify(jsonObject);

    fetch(form.getAttribute('action') , {
        method: form.getAttribute('method'),
        headers: { 'Content-Type': 'application/json' },
        body: jsonString
    })
    .then(response => {
        if (response.ok) {
            return response;
        }
        return Promise.reject(response);
    })
    .then(result => {
        window.location.reload();
    })
    .catch((response) => {
        console.error(response.status, response.statusText);

        const message = response.headers.get("X-Status-Message");
        showWarning(message);

//        response.json().then((json) => {
//            console.log(json);
//        })

    });
}

async function getJson(uri) {

    console.log(uri);

    return await fetch(uri , {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    })
    .then(response => {
        console.log(response.ok);
        if (response.ok) {
            return response.json();
        }
        return Promise.reject(response);
    })
    .then(result => {
        console.log(result);
        return result;
    })
    .catch((response) => {
        console.error(response.status, response.statusText);

        const message = response.headers.get("X-Status-Message");
        showWarning(message);
    });
}

function showWarning(message) {
    document.getElementById('warning').innerText = message;
    document.getElementById('warning').hidden = false;
}