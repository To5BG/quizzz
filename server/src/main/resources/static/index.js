/*------------------------- EVENT LISTENERS -------------------------*/

let basePath = "http://localhost:8080/api/activities";

addEventListener("load", _ => {
    for (let input of document.querySelectorAll("#inputCont input, #inputCont textarea")) {
        input.setAttribute("disabled", '');
    }
    document.querySelector("#inputCont > #addOne > form")
        .addEventListener('submit', postActivity);

    document.querySelector("#inputCont > #addOneJson > form")
        .addEventListener('submit', postActivityJson);

    document.querySelector("#inputCont > #addJsonFile")
        .addEventListener('submit', postJsonFile);

    document.querySelector("#inputCont > #editOne > form")
        .addEventListener('submit', editActivity);

    document.querySelector("#refreshDB")
        .addEventListener('click', refreshTable);

    document.querySelector("#resetDB")
        .addEventListener('click', resetDatabase);

    document.querySelector("#removeOne > form")
        .addEventListener('submit', removeActivity)
    refreshTable();

    [...document.querySelectorAll('input[type="text"],input[type="file"],textarea')]
        .forEach(el => el.setAttribute("required", ''));
});

/*----------------------- TOGGLE FUNCTIONS --------------------------*/

function toggleContainer(contName) {
    let children = document.getElementById("btnCont").children;
    for (let container of children) {
        if (container.id === contName)
            container.style.setProperty("opacity",
                (1 - container.style.getPropertyValue("opacity")).toString());
        else container.style.setProperty("opacity", "0");
    }
    for (let sibling of document.querySelectorAll("#inputCont > div")) {
        sibling.style.setProperty("opacity", "0");
    }
}

function toggleInputForm(contName) {
    let children = document.getElementById("inputCont").children;
    for (let container of children) {
        if (container.id === contName) {
            let newOpacity = 1 - parseInt(container.style.getPropertyValue("opacity"));
            container.style.setProperty("opacity", newOpacity.toString());

            for (let input of container.querySelectorAll("input,textarea")) {
                if (newOpacity === 0) input.setAttribute("disabled", '');
                else input.removeAttribute("disabled");
            }
        } else {
            container.style.setProperty("opacity", "0");
            for (let input of container.querySelectorAll("input,textarea")) {
                input.setAttribute("disabled", '');
            }
        }
    }
}

/*------------------------ ADD ACTIVITIES ---------------------------*/

async function send(activityData) {
    let url = basePath;
    return await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: activityData
    });
}

function postActivity(event) {
    event.preventDefault();

    const data = new FormData(event.target);
    const values = Object.fromEntries(data.entries());
    let alertMsg = document.querySelector("#alertMsg");

    send(JSON.stringify(values))
        .then(response => {
            if (response.status === 400) {
                console.log("Bad request!");
                alertMsg.textContent = "Addition was unsuccessful!";
                alertMsg.style.setProperty("color", "red");
            } else return response.json()
                .then(_ => {
                    alertMsg.textContent = "Addition was successful!";
                    alertMsg.style.setProperty("color", "green");
                    refreshTable();
                }, err => {
                    console.log("Error! " + err);
                    alertMsg.textContent = "Addition was unsuccessful!";
                    alertMsg.style.setProperty("color", "red");
                });
        });
}

function postActivityJson(event) {
    event.preventDefault();

    const text = document.querySelector("textarea").value;
    let alertMsg = document.querySelector("#alertMsg");
    send(text.replace(/"id": ".*",/, "\"id\": null,"))
        .then(response => {
            if (response.status === 400) {
                console.log("Bad request!");
                alertMsg.textContent = "Addition was unsuccessful!";
                alertMsg.style.setProperty("color", "red");
            } else return response.json()
                .then(data => {
                    alertMsg.textContent = "Addition was successful! Just added " + data;
                    alertMsg.style.setProperty("color", "green");
                    refreshTable();
                }, err => {
                    console.log("Error! " + err);
                    alertMsg.textContent = "Addition was unsuccessful!";
                    alertMsg.style.setProperty("color", "red");
                });
        });

}

function postJsonFile(event) {
    event.preventDefault();

    let alertMsg = document.querySelector("#alertMsg");
    let fileReader = new FileReader();
    let success = 0;
    let fail = 0;
    fileReader.onload = function () {
        let res = fileReader.result;
        try {
            res = JSON.parse(fileReader.result);
            let arr = [...res];
            new Promise(function (resolve, reject) {
                arr.forEach((activity, _) => {
                    if (activity.id) activity.id = null;
                    send(JSON.stringify(activity))
                        .then(res => {
                            if (res.status === 200) ++success;
                            else ++fail;
                        }, err => {
                            console.log("Error! " + err);
                            alertMsg.textContent = "Failed to load some entries!";
                            alertMsg.style.setProperty("color", "red");
                        });
                });
                let handler = setInterval(_ => {
                    if (success + fail === arr.length) {
                        resolve(success);
                        clearInterval(handler);
                    }
                }, 100);
            }).then(res => {
                refreshTable();
                alertMsg.textContent = "Bulk addition was successful! Added " + res + " entries.";
                alertMsg.style.setProperty("color", "green");
            });
        } catch {
            alertMsg.textContent = "Could not parse file!";
            alertMsg.style.setProperty("color", "red");
        }
    }
    fileReader.readAsText(event.target.querySelector("input").files[0]);
}

/*------------------------ EDIT ACTIVITY --------------------------*/

async function editActivity(event) {
    event.preventDefault();

    const id = document.querySelector("#editId");
    const data = new FormData(event.target);
    const values = Object.fromEntries(data.entries());
    let alertMsg = document.querySelector("#alertMsg");

    updateEditedActivity(values, id)
        .then(response => {
            if (response.status === 400) {
                console.log("Bad request!");
                alertMsg.textContent = "Addition was unsuccessful!";
                alertMsg.style.setProperty("color", "red");
            } else return response.json()
                .then(_ => {
                    alertMsg.textContent = "Addition was successful!";
                    alertMsg.style.setProperty("color", "green");
                    refreshTable();
                }, err => {
                    console.log("Error! " + err);
                    alertMsg.textContent = "Addition was unsuccessful!";
                    alertMsg.style.setProperty("color", "red");
                });
        });
}

async function updateEditedActivity(activityData, id) {
    let url = basePath + "/" + id;
    return await fetch(url, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: activityData
    });
}

/*------------------------ REMOVE ACTIVITY --------------------------*/

async function remove(id) {
    let url = basePath + "/" + id;
    return await fetch(url, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    });
}

async function removeActivity(event) {
    event.preventDefault();
    const id = document.querySelector("#removeId").value;
    let alertMsg = document.querySelector("#alertMsg");

    remove(id)
        .then(response => {
            if (response.status === 400) {
                console.log("Bad request!");
                alertMsg.textContent = "Removal was unsuccessful! Make sure you enter a valid id.";
                alertMsg.style.setProperty("color", "red");
            } else if (response.status === 404) {
                console.log("Not found!");
                alertMsg.textContent = "Removal was unsuccessful! An entry with the provided id was not found.";
                alertMsg.style.setProperty("color", "red");
            } else {
                alertMsg.textContent = "Removal was successful!";
                alertMsg.style.setProperty("color", "green");
                refreshTable();
            }
        }, err => {
            console.log("Error! " + err);
            alertMsg.textContent = "Removal was unsuccessful!";
            alertMsg.style.setProperty("color", "red");
        });
}

/*-------------------------- VIEW DATABASE --------------------------*/

function refreshTable() {
    let alertMsg = document.querySelector("#alertMsg");

    fetchDatabase()
        .then(data => {
            let table = document.querySelector("table");
            // Recreates table anew - not ideal, but unwilling to optimize this for now,
            // we are handling a small amount of entries anyway
            while (table.childElementCount !== 1) {
                table.removeChild(table.lastChild);
            }
            for (let entry of data) {
                let tablerow = document.createElement("tr");

                let tableIdColumn = document.createElement("td");
                tableIdColumn.textContent = entry.id;
                tablerow.append(tableIdColumn)

                let tableTitleColumn = document.createElement("td");
                tableTitleColumn.textContent = entry.title;
                tablerow.append(tableTitleColumn)

                let tableConsumptionColumn = document.createElement("td");
                tableConsumptionColumn.textContent = entry.consumption_in_wh;
                tablerow.append(tableConsumptionColumn);

                let tableImagePathColumn = document.createElement("td");
                tableImagePathColumn.textContent = entry.image_path;
                tablerow.append(tableImagePathColumn);

                let tableSourceColumn = document.createElement("td");
                tableSourceColumn.textContent = entry.source;
                tablerow.append(tableSourceColumn);

                table.append(tablerow);
            }
            document.querySelector("#entryCounter").textContent = data.length;
        }, err => {
            console.log("Could not fetch activities!" + err);
            alertMsg.textContent = "Could not connect to the database!";
            alertMsg.style.setProperty("color", "red");
        });
}

async function fetchDatabase() {
    let url = basePath;
    let response = await fetch(url, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    });
    return response.json();
}

/*------------------------ RESET DATABASE ---------------------------*/

async function deleteRequest() {
    let url = basePath;
    return await fetch(url, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    });
}

function resetDatabase() {
    let alertMsg = document.querySelector("#alertMsg");

    deleteRequest()
        .then(response => {
            if (response.status === 400) {
                alertMsg.textContent = "Could not connect to the database!";
                alertMsg.style.setProperty("color", "red");
            } else {
                alertMsg.textContent = "Reset successful!";
                alertMsg.style.setProperty("color", "green");
                refreshTable();
            }
        }, err => {
            console.log("Error!" + err)
        });
}


