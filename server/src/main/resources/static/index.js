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

    document.querySelector("#refreshDB")
        .addEventListener('click', refreshTable);

    document.querySelector("#resetDB")
        .addEventListener('click', resetDatabase);

    document.querySelector("#removeOne > form")
        .addEventListener('submit', removeActivity)
    refreshTable();
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
                })
                .catch((err) => {
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

    send(text)
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
                })
                .catch((err) => {
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
    fileReader.onload = function () {
        let res = fileReader.result;
        try {
            res = JSON.parse(fileReader.result);
            new Promise(function (resolve, reject) {
                [...res].forEach((activity, _) => {
                    if (activity.id) activity.id = null;
                    send(JSON.stringify(activity))
                        .then(res => {
                            return (res.status === 200) ? ++success : success;
                        })
                        .catch((err) => {
                            console.log("Error! " + err);
                            alertMsg.textContent = "Could not parse file!";
                            alertMsg.style.setProperty("color", "red");
                        });
                });
                setTimeout(() => resolve(success), 500);
            }).then(_ => {
                refreshTable();
                alertMsg.textContent = "Bulk addition was successful! Added " + success + " entries.";
                alertMsg.style.setProperty("color", "green");
            });
        } catch {
            alertMsg.textContent = "Could not parse file!";
            alertMsg.style.setProperty("color", "red");
        }
    }
    fileReader.readAsText(event.target.querySelector("input").files[0]);
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
                alertMsg.textContent = "Removal was unsuccessful!";
                alertMsg.style.setProperty("color", "red");
            } else {
                alertMsg.textContent = "Removal was successful!";
                alertMsg.style.setProperty("color", "green");
                refreshTable();
            }
        })
        .catch((err) => {
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
        })
        .catch((err) => {
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

    deleteRequest().then(response => {
        if (response.status === 400) {
            alertMsg.textContent = "Could not connect to the database!";
            alertMsg.style.setProperty("color", "red");
        } else return response.json()
            .then(_ => {
                alertMsg.textContent = "Reset successful!";
                alertMsg.style.setProperty("color", "green");
                refreshTable();
            })
            .catch((err) => {
                console.log("Error!" + err)
            });
    })
}


