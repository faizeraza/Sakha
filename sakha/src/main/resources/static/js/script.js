const chatBox = document.getElementById('chatBox');
let conversationStep = 0;
let taskid, fieldName, newValue;
let task = {
    taskname: '',
    description: '',
    scheduleDate: '',
    scheduleTime: '',
    status: ''
};

const inputBox = document.getElementById("userInput");
const btn = document.getElementById("btn");
inputBox.addEventListener("keypress", () => {
    if (event.key === "Enter") {
        event.preventDefault();
        btn.click();
    }
})

// Updated Changes 
function appendMessage(content, isUser) {
    const messageDiv = document.createElement('div');
    messageDiv.classList.add('chat-message', isUser ? 'user' : 'bot');

    const messageContent = document.createElement('p');
    messageContent.innerHTML = content;  // Use innerHTML to allow HTML content
    messageDiv.appendChild(messageContent);

    chatBox.appendChild(messageDiv);
    chatBox.scrollTop = chatBox.scrollHeight;  // Automatically scroll to bottom
}

function sendMessage() {
    const input = document.getElementById('userInput');
    const message = input.value;
    if (message.trim() !== "") {
        appendMessage(message, true); // User message
        input.value = '';
        handleBotResponse(message);
    }
}

function handleKeyPress(event) {
    if (event.key === 'Enter') {
        sendMessage(); // Call sendMessage function when Enter is pressed
    }
}

function displayOptions() {
    appendMessage("What would you like to do?<br>1: Create a task<br>2: List all tasks<br>3: Update a task<br>4: Delete a task", false);
    conversationStep = 0; // Reset conversation step to show options
}

function handleBotResponse(userMessage) {
    switch (conversationStep) {
        case 0:
            if (userMessage === '1') {
                appendMessage('You chose to create a task. Please provide the task name:', false);
                conversationStep = 1; // Move to the next step (task name)
            } else if (userMessage === '2') {
                appendMessage('Fetching all tasks...', false);
                listTasks(); // Call to list all tasks
            } else if (userMessage === '3') {
                appendMessage('Please provide the task Id to update:', false);
                conversationStep = 10; // Move to update flow
            } else if (userMessage === '4') {
                appendMessage('Please provide the task Id to delete:', false);
                conversationStep = 20; // Move to delete flow
            } else {
                appendMessage('Sorry, I did not understand that. Please select an option (1-4).', false);
            }
            break;

        // Task creation steps
        case 1:
            task.taskname = userMessage;
            appendMessage('Got it! Now provide a brief description for the task:', false);
            conversationStep = 2; // Move to description input
            break;

        case 2:
            task.description = userMessage;
            appendMessage('Great! Now, select a date for the task (YYYY-MM-DD):', false);
            conversationStep = 3; // Move to date input
            break;

        case 3:
            task.scheduleDate = userMessage;
            appendMessage('Now select a time for the task (HH:mm):', false);
            conversationStep = 4; // Move to time input
            break;

        case 4:
            task.scheduleTime = userMessage;
            appendMessage('Finally, is the task complete or incomplete? (Type "Complete" or "Incomplete")', false);
            conversationStep = 5; // Move to status input
            break;

        case 5:
            task.status = userMessage.toLowerCase() === 'complete'; // Boolean value
            appendMessage('Please confirm: Do you want to create this task? (yes/no)', false);
            conversationStep = 6; // Move to task confirmation
            break;

        case 6:
            if (userMessage.toLowerCase() === 'yes') {
                createTask(task); // Call function to create the task
            } else {
                appendMessage('Task creation cancelled. ', false);
                displayOptions(); // Show options again
                conversationStep = 0; // Reset for next operation
                task = {}; // Reset task data
            }
            break;

        // Task update steps
        case 10:
            taskid = userMessage; // Save task name
            appendMessage(`You want to update the task: ${taskid}. Please confirm (yes/no):`, false);
            conversationStep = 11; // Move to task update confirmation
            break;

        case 11:
            if (userMessage.toLowerCase() == 'yes') {
                appendMessage('What field would you like to update ? ', false);
                conversationStep = 12; // Move to description input step
            } else {
                appendMessage('Task update cancelled.', false);
                displayOptions(); // Show options again
                conversationStep = 0; // Reset for next operation
            }
            break;

        case 12:
            fieldName = userMessage;
            appendMessage('Enter the updated Value :- ', false);
            // updateTaskField(id,fieldName,newValue); 
            conversationStep = 13;
            // Now call the update function with the completed task data
            break;

        case 13:
            newValue = userMessage; // Save the description provided by the user
            console.log(newValue);
            appendMessage(fieldName + ' Updated. Proceeding with task update...', false);
            updateTask(taskid, fieldName, newValue);
            break;
        // Task deletion steps
        case 20:
            task.taskname = userMessage;
            appendMessage(`Are you sure you want to delete the task: ${userMessage}? (yes/no)`, false);
            conversationStep = 21; // Move to task deletion confirmation
            break;

        case 21:
            if (userMessage.toLowerCase() === 'yes') {
                deleteTask(task.taskname); // Call function to delete the task
            } else {
                appendMessage('Task deletion cancelled.', false);
                displayOptions(); // Show options again
                conversationStep = 0; // Reset for next operation
            }
            break;

        default:
            appendMessage('Sorry, something went wrong.', false);
    }
}

// Function to create the task via AJAX
function createTask(taskData) {
    fetch('/tasks/create', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(taskData)
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Failed to create task');
        })
        .then(data => {
            appendMessage(`Task "${data.taskname}" created successfully!`, false);
            displayOptions(); // Show options again
            conversationStep = 0; // Reset for next operation
            task = {}; // Reset task data
        })
        .catch(error => {
            appendMessage(`Error: ${error.message}`, false);
            displayOptions(); // Show options again
            conversationStep = 0; // Reset conversation for next operation
            task = {}; // Reset task data
        });
}

// Updates Changes [ ListTasks]

function listTasks() {
    fetch('/tasks/all')
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Failed to fetch tasks');
        })
        .then(data => {
            if (data.length === 0) {
                appendMessage('No tasks found.', false);
            } else {
                // Create a table element with improved styles
                let table = `<table style="width: 100%; border-collapse: collapse; margin: 10px 0; font-size: 1em; font-family: Arial, sans-serif; box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);">
                                    <thead>
                                        <tr style="background-color: #007BFF; color: white; text-align: left;">
                                            <th style="padding: 10px;">ID</th>
                                            <th style="padding: 10px;">Task Name</th>
                                            <th style="padding: 15px;">Description</th>
                                            <th style="padding: 10px;">Scheduled Time</th>
                                            <th style="padding: 10px;">Scheduled Date</th>
                                            <th style="padding: 10px;">Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>`;

                data.forEach((task, index) => {
                    table += `<tr style="border-bottom: 1px solid #dddddd;">
                                    <td style="padding: 10px; text-align: center;">${task.id}</td>
                                    <td style="padding: 10px;">${task.taskname}</td>
                                    <td style="padding: 10px;">${task.description}</td>
                                    <td style="padding: 15px; text-align: center;">${task.scheduleTime}</td>
                                    <td style="padding: 10px; text-align: center;">${task.scheduleDate}</td>
                                    <td style="padding: 10px; text-align: center;">${task.status}</td>
                                  </tr>`;
                });

                table += `</tbody></table>`;

                appendMessage(table, false); // Display the task table
            }
            displayOptions(); // Show options again
            conversationStep = 0; // Reset for next operation
        })
        .catch(error => {
            appendMessage(`Error: ${error.message}`, false);
            displayOptions(); // Show options again
            conversationStep = 0; // Reset conversation for next operation
        });
}


// Updated ListTasks Method 
// Function to list all tasks by fetching from the backend
function listTasks() {
    fetch('/tasks/all')
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Failed to fetch tasks');
        })
        .then(data => {
            if (data.length === 0) {
                appendMessage('No tasks found.', false);
            } else {
                // Create a table element with improved styles
                let table = `<table style="width: 100%; border-collapse: collapse; margin: 10px 0; font-size: 1em; font-family: Arial, sans-serif; box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);">
                                <thead>
                                    <tr style="background-color: #007BFF; color: white; text-align: left;">
                                        <th style="padding: 10px;">ID</th>
                                        <th style="padding: 10px;">Task Name</th>
                                        <th style="padding: 15px;">Description</th>
                                        <th style="padding: 10px;">Scheduled Time</th>
                                        <th style="padding: 10px;">Scheduled Date</th>
                                        <th style="padding: 10px;">Status</th>
                                    </tr>
                                </thead>
                                <tbody>`;

                data.forEach((task) => {
                    table += `<tr style="border-bottom: 1px solid #dddddd;">
                                <td style="padding: 10px; text-align: center;">${task.id}</td>
                                <td style="padding: 10px;">${task.taskname}</td>
                                <td style="padding: 10px;">${task.description}</td>
                                <td style="padding: 15px; text-align: center;">${task.scheduleTime}</td>
                                <td style="padding: 10px; text-align: center;">${task.scheduleDate}</td>
                                <td style="padding: 10px; text-align: center;">${task.status}</td>
                              </tr>`;
                });

                table += `</tbody></table>`;

                appendMessage(table, true); // Display the task table with HTML formatting
            }
            displayOptions(); // Show options again
            conversationStep = 0; // Reset for next operation
        })
        .catch(error => {
            appendMessage(`Error: ${error.message}`, false);
            displayOptions(); // Show options again
            conversationStep = 0; // Reset conversation for next operation
        });
}






// Function to update the task via AJAX
// function updateTask(taskData) {

//     // const fieldName = taskData.fieldName;  // Capture which field to update
//     const fieldName = prompt("Enter your Field that you want to update :");
//     const newValue = taskData.newValue;    // Capture the new value

//     fetch(`/tasks/update/${encodeURIComponent(taskData.id)}?fieldName=${encodeURIComponent(fieldName)}&newValue=${encodeURIComponent(newValue)}`, {
//         method: 'PUT',
//         headers: {
//             'Content-Type': 'application/json'
//         }
//     })
//     .then(response => {
//         if (response.ok) {
//             return response.json();
//         }
//         throw new Error('Failed to update task');
//     })
//     .then(data => {
//         appendMessage(`Task updated successfully!`, false);
//         displayOptions(); // Show options again
//         conversationStep = 0; // Reset for next operation
//     })
//     .catch(error => {
//         appendMessage(`Error: ${error.message}`, false);
//         displayOptions(); // Show options again
//         conversationStep = 0; // Reset conversation for next operation
//     });
// }


// **Updated the UpdateTaskData **

// Function to update the task via AJAX
// Function to update a task by sending a PUT request
function updateTask(taskid, fieldName, newValue) {
    console.log(fieldName);
    console.log(newValue);
    if (['name', 'description', 'date', 'time', 'status'].includes(fieldName)) {

        if (newValue) {
            fetch(`/tasks/update/${encodeURIComponent(taskid)}?fieldName=${encodeURIComponent(fieldName)}&newValue=${encodeURIComponent(newValue)}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/text'
                }
            })
                .then(response => {
                    if (response.ok) {
                        return response.text();
                    }
                    throw new Error('Failed to update task');
                })
                .then(data => {
                    appendMessage(`Task updated successfully!`, false);
                    displayOptions(); // Show options again
                    conversationStep = 0; // Reset for next operation
                })
                .catch(error => {
                    appendMessage(`Error: ${error.message}`, false);
                    console.log(error.message)
                    displayOptions(); // Show options again
                    conversationStep = 0; // Reset conversation for next operation
                });
        } else {
            appendMessage("New value was not provided.", false);
            displayOptions(); // Show options again
            conversationStep = 0; // Reset conversation for next operation
        }

        // document.getElementById("user-input").removeEventListener("change", handleValueChange); // Remove listener after capturing value
    }
    else {
        appendMessage('Invalid field. Please specify a valid field (name/description/date/time/status):', false);
    }
}

// Function to delete the task via AJAX
function deleteTask(taskName) {
    fetch(`/tasks/delete/${encodeURIComponent(taskName)}`, {
        method: 'DELETE',
    })
        .then(response => {
            if (response.ok) {
                appendMessage(`Task "${taskName}" deleted successfully!`, false);
            } else {
                throw new Error('Failed to delete task');
            }
            displayOptions(); // Show options again
            conversationStep = 0; // Reset for next operation
        })
        .catch(error => {
            appendMessage(`Error: ${error.message}`, false);
            displayOptions(); // Show options again
            conversationStep = 0; // Reset conversation for next operation
        });
}

// Updated the Delete task 
function deleteTask(taskName) {
    // Ask for user confirmation before deleting the task
    const confirmDeletion = confirm(`Are you sure you want to delete the task "${taskName}"?`);

    if (confirmDeletion) {
        fetch(`/tasks/delete/${encodeURIComponent(taskName)}`, {
            method: 'DELETE',
        })
            .then(response => {
                if (response.ok) {
                    appendMessage(`Task "${taskName}" deleted successfully!`, false);
                } else {
                    throw new Error('Failed to delete task');
                }
                displayOptions(); // Show options again
                conversationStep = 0; // Reset for next operation
            })
            .catch(error => {
                appendMessage(`Error: ${error.message}`, false);
                displayOptions(); // Show options again
                conversationStep = 0; // Reset conversation for next operation
            });
    } else {
        appendMessage(`Task deletion cancelled.`, false);
        displayOptions(); // Show options again
        conversationStep = 0; // Reset conversation for next operation
    }
}

function executeCommand(command) {

}
