let dateTime;

const container = document.querySelector(".grid-container");

fetch('http://localhost:8080/tasks/all')
    .then(response => {
        if (response.ok) {
            return response.json();
        } else
            throw new Error('Failed to fetch tasks');
    })
    .then(data => {
        console.log(data);

        data.forEach((task) => {
            console.log(task);
            listTasks(task.taskname);

        })
    });

function listTasks(taskname) {
    const item = document.createElement("div");
    item.className = "grid-item";
    item.textContent = taskname;
    item.appendChild(document.createElement("br"));
    const btn1 = document.createElement("button");
    btn1.className = "btn";
    btn1.innerHTML = "Execute";
    btn1.addEventListener("click", () => handleExecute(taskname));
    item.appendChild(btn1);
    const btn2 = document.createElement("button");
    btn2.className = "btn";
    btn2.innerHTML = "Schedule";
    btn2.addEventListener("click", handleSchedule);
    item.appendChild(btn2);
    container.appendChild(item);
}

function handleExecute(taskname) {
    console.log("IN");
    
    fetch(`http://localhost:8080/tasks/execute?taskname=${taskname}`)
        .then(response => {
            if (response.ok) {
                console.log(response);
                ;
            } else
                throw new Error('Failed to fetch tasks');
        })
}

function handleSchedule() {
    dateTime = prompt("Enter date time");
}
