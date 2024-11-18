let currentStep = 0; // Track the current interaction step
let taskData = {}; // Temporary storage for task details
let currentAction = ""; // Tracks the current action (execute or schedule)

// Add messages to chat box
function addMessage(text, sender) {
    const chatBox = document.getElementById("chatBox");
    const messageDiv = document.createElement("div");
    messageDiv.classList.add("message", sender);
    messageDiv.innerHTML = `<p>${text}</p>`;
    chatBox.appendChild(messageDiv);
    chatBox.scrollTop = chatBox.scrollHeight; // Scroll to bottom
}

// Show options for executing tasks
function showExecuteTaskOptions() {
    addMessage("You chose to execute a task. Please choose an option:", "bot");
    listTasks();
}

// Show options for creating a task
function showCreateTaskOptions() {
    addMessage("You chose to create a new task. Please provide the task details.", "bot");
    setOptions(["Create and Execute Now", "Create and Schedule Later"]);
    currentStep = 2;
}

// Display available options as buttons
function setOptions(options) {
    const chatBox = document.getElementById("chatBox");
    const optionsDiv = document.createElement("div");
    optionsDiv.classList.add("options");

    options.forEach(option => {
        const button = document.createElement("button");
        button.textContent = option;
        button.onclick = () => handleOptionSelection(option);
        optionsDiv.appendChild(button);
    });

    chatBox.appendChild(optionsDiv);
    chatBox.scrollTop = chatBox.scrollHeight; // Scroll to bottom
}

// Handle option selection
function handleOptionSelection(option) {
    if (currentStep === 1) {
        if (option === "List tasks") {
            listTasks();
        } else if (option === "Execute a task") {
            promptForTaskName("execute");
        } else if (option === "Schedule a task") {
            promptForTaskName("schedule");
        }
    } else if (currentStep === 2) {
        if (option === "Create and Execute Now") {
            currentAction = "execute";
            promptForTaskDetails();
        } else if (option === "Create and Schedule Later") {
            currentAction = "schedule";
            promptForTaskDetails();
        }
    }
}

// // List tasks from the backend
// async function listTasks() {
//     try {
//         const response = await fetch('http://localhost:8080/tasks/all');
//         const tasks = await response.json();
//         let taskList = "<p>Here are the available tasks:</p>";
//         taskList += `
//             <table>
//                 <tr>
//                     <th>Task Name</th>
//                     <th>Command</th>
//                     <th>Actions</th>
//                 </tr>`;
        
//         tasks.forEach(task => {
//             taskList += `
//                 <tr>
//                     <td>${task.taskname}</td>
//                     <td>${task.command}</td>
//                     <td>
//                         <div class="action-buttons">
//                             <button onclick="executeTask('${task.taskname}')">Execute</button>
//                             <button onclick="scheduleTask('${task.taskname}')">Schedule</button>
//                         </div>
//                     </td>
//                 </tr>`;
//         });
//         taskList += "</table>";
//         addMessage(taskList, "bot");
//     } catch (error) {
//         addMessage("Failed to fetch tasks. Please try again later.", "bot");
//     }
// }

// Updated listTasks function to attach event listeners after creating buttons
async function listTasks() {
    try {
        const response = await fetch('http://localhost:8080/tasks/all');
        const tasks = await response.json();
        let taskList = "<p>Here are the available tasks:</p>";
        taskList += `
            <table>
                <tr>
                    <th>Task Name</th>
                    <th>Command</th>
                    <th>Actions</th>
                </tr>`;
        
        tasks.forEach(task => {
            taskList += `
                <tr>
                    <td>${task.taskname}</td>
                    <td>${task.command}</td>
                    <td>
                        <div class="action-buttons">
                            <button class="execute-btn" data-taskname="${task.taskname}">Execute</button>
                            <button class="schedule-btn" data-taskname="${task.taskname}">Schedule</button>
                        </div>
                    </td>
                </tr>`;
        });
        taskList += "</table>";
        addMessage(taskList, "bot");

        // Attach event listeners for the newly created buttons
        document.querySelectorAll(".execute-btn").forEach(button => {
            button.onclick = () => executeTask(button.getAttribute("data-taskname"));
        });

        document.querySelectorAll(".schedule-btn").forEach(button => {
            button.onclick = () => promptForDateTime(button.getAttribute("data-taskname"));
        });

    } catch (error) {
        addMessage("Failed to fetch tasks. Please try again later.", "bot");
    }
}


// Execute a task by taskname
async function executeTask(taskname) {
    try {
        const response = await fetch(`http://localhost:8080/tasks/execute?taskname=${taskname}`);
        const result = await response.text();
        addMessage(`Task executed: ${result}`, "bot");
    } catch (error) {
        addMessage("Failed to execute task. Please try again.", "bot");
    }
}

// Prompt user for task details
function promptForTaskDetails() {
    addMessage("Please provide the task name:", "bot");
    currentStep = 3;
}

// Handle user input for task details step-by-step
function handleTaskDetailsInput(input) {
    if (currentStep === 3) {
        taskData.taskname = input;
        addMessage("Please provide the command:", "bot");
        currentStep = 4;
    } else if (currentStep === 4) {
        taskData.command = input;
        addMessage("Please provide a description:", "bot");
        currentStep = 5;
    } else if (currentStep === 5) {
        taskData.description = input;
        if (currentAction === "execute") {
            createAndExecuteTask();
        } else if (currentAction === "schedule") {
            promptForDateTime(taskData.taskname);
        }
    }
}

// Create and execute a task
async function createAndExecuteTask() {
    addMessage("Creating the task...", "bot");
    try {
        // Step 1: Create the task
        const createResponse = await fetch('http://localhost:8080/tasks/create', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(taskData)
        });

        if (!createResponse.ok) {
            throw new Error("Failed to create task.");
        }

        const createdTask = await createResponse.json();
        addMessage(`Task "${createdTask.taskname}" created successfully. Executing now...`, "bot");

        // Step 2: Execute the task
        const executeResponse = await fetch(`http://localhost:8080/tasks/execute?taskname=${taskData.taskname}`, {
            method: 'GET',
        });

        if (!executeResponse.ok) {
            throw new Error("Failed to execute task.");
        }

        const executionResult = await executeResponse.text();
        addMessage(`Task executed successfully: ${executionResult}`, "bot");
    } catch (error) {
        addMessage(`Error: ${error.message}`, "bot");
    } finally {
        taskData = {}; // Reset task data
        currentStep = 0; // Reset step
    }
}

// Prompt user for date and time
function promptForDateTime(taskname) {
    const dateInput = document.createElement("input");
    dateInput.type = "date";
    const timeInput = document.createElement("input");
    timeInput.type = "time";

    const scheduleButton = document.createElement("button");
    scheduleButton.textContent = "Schedule Task";
    scheduleButton.onclick = async () => {
        const dateTime = `${dateInput.value}T${timeInput.value}:00`;
        scheduleTaskWithDateTime(taskname, dateTime);
    };

    addMessage("Please select date and time:", "bot");
    document.body.append(dateInput, timeInput, scheduleButton);
}

// Schedule a task with selected date and time
async function scheduleTaskWithDateTime(taskname, dateTime) {
    console.log(`Scheduling task: ${taskname} at ${dateTime}`);

    if (!dateTime.trim() || dateTime.includes("Invalid")) {
                    addMessage("Please provide a valid date and time.", "bot");
                    return;
                }
    try {
        await fetch('http://localhost:8080/scheduledTask/create', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ taskname, dateTime : dateTime, status: "pending" })
        });
        addMessage(`Task scheduled for ${dateTime}.`, "bot");
    } catch (error) {
        addMessage("Failed to schedule task. Please try again.", "bot");
    }
}

// // Schedule a task with selected date and time
// async function scheduleTaskWithDateTime(taskname, dateTime) {
//     try {
//         // Ensure the dateTime value is formatted as expected
//         console.log(`Scheduling task: ${taskname} at ${dateTime}`);

//         // Validate that both date and time inputs are not empty
//         if (!dateTime.trim() || dateTime.includes("Invalid")) {
//             addMessage("Please provide a valid date and time.", "bot");
//             return;
//         }

//         // Send the scheduling request to the backend
//         const response = await fetch('http://localhost:8080/scheduledTask/create', {
//             method: 'POST',
//             headers: { 'Content-Type': 'application/json' },
//             body: JSON.stringify({ taskname, dateTime, status: "pending" })
//         });

//         if (!response.ok) {
//             throw new Error("Failed to schedule the task.");
//         }

//         const result = await response.json();
//         addMessage(`Task "${taskname}" scheduled for ${dateTime}.`, "bot");
//         console.log(`Task scheduled successfully: ${JSON.stringify(result)}`);
//     } catch (error) {
//         addMessage(`Failed to schedule task. Error: ${error.message}`, "bot");
//         console.error(error);
//     }
// }


// Handle user input
function handleUserInput() {
    const input = document.getElementById("userInput").value;
    if (input.trim() !== "") {
        addMessage(input, "user");
        document.getElementById("userInput").value = "";

        // Handle task details input
        if (currentStep >= 3 && currentStep <= 5) {
            handleTaskDetailsInput(input);
        }
    }
}
