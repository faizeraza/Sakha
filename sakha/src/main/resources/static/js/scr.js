let currentStep = 0; // Track the current interaction step

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
    setOptions(["List tasks", "Execute a task", "Schedule a task"]);
    currentStep = 1;
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
            promptForTaskDetails("execute");
        } else if (option === "Create and Schedule Later") {
            promptForTaskDetails("schedule");
        }
    }
}

// List tasks from the backend
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
                            <button onclick="executeTask('${task.taskname}')">Execute</button>
                            <button onclick="scheduleTask('${task.taskname}')">Schedule</button>
                        </div>
                    </td>
                </tr>`;
        });
        taskList += "</table>";
        addMessage(taskList, "bot");
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

// Schedule a task by taskname
function scheduleTask(taskname) {
    addMessage("Please choose a date and time for scheduling.", "bot");
    promptForDateTime(taskname);
}

// Prompt user for task details
function promptForTaskDetails(action) {
    addMessage("Please enter the task name, command, and description.", "bot");
    currentStep = 3;
}

// Handle user input for task details
async function handleTaskDetailsInput(taskDetails, action) {
    if (action === "schedule") {
        promptForDateTime(taskDetails);
    } else {
        await createTask(taskDetails);
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
        const dateTime = `${dateInput.value}T${timeInput.value}`;
        scheduleTaskWithDateTime(taskname, dateTime);
    };

    addMessage("Please select date and time:", "bot");
    document.body.append(dateInput, timeInput, scheduleButton);
}

// Schedule a task with selected date and time
async function scheduleTaskWithDateTime(taskname, dateTime) {
    try {
        await fetch('http://localhost:8080/scheduledTask/create', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ taskname, scheduleDateTime: dateTime })
        });
        addMessage(`Task scheduled for ${dateTime}.`, "bot");
    } catch (error) {
        addMessage("Failed to schedule task. Please try again.", "bot");
    }
}

// Handle user input
function handleUserInput() {
    const input = document.getElementById("userInput").value;
    if (input.trim() !== "") {
        addMessage(input, "user");
        document.getElementById("userInput").value = "";
    }
}
