# Task Scheduler

A simple task scheduling application that allows users to add, view, and manage scheduled tasks.

![Workflow Diagram](https://github.com/faizeraza/Sakha/blob/main/sakha/src/main/resources/assets/flowv1.png)

## Table of Contents
- [Introduction](#introduction)
- [Installation](#installation)
- [Usage](#usage)
- [API Documentation](#api-documentation)
- [Workflow](#workflow)
- [Contributing](#contributing)
- [License](#license)

## Introduction
This project provides a RESTful API for scheduling tasks using cron jobs. Users can add tasks with specific execution times and manage their statuses. The application is built using Java and Spring Boot.

## Installation
### Prerequisites
- Java 11 or higher
- Maven

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/task-scheduler.git

2. Navigate to the project directory:
    ```bash
    cd task-scheduler

3. demo json
   '''bash
   {

    "taskname": "wifi on",

    "dateTime": "2024-11-12T14:18:00",

    "status": "pending"

}
