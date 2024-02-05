# Messenger

This Messenger is a simple JavaFX based messaging client that connects to a messaging server, allowing users to send and receive text messages and files.

## Features

- Connect to a messaging server using TCP/IP protocol.
- Send and receive real-time text messages.
- Send and receive files.
- Simple and intuitive graphical user interface.

## Prerequisites

Before you can run the messenger client, make sure you have the following installed:
- Java SE Runtime Environment 8 or higher
- JavaFX SDK (compatible with your JRE version)
- Maven (to manage dependencies and run the application).

You can check if Java and Maven are installed by running the following commands in the terminal:

`java -version`
`mvn -version`

## Installation

To set up the messenger client on your local machine, follow these steps:

1. Clone the repository to your local machine using Git commands or download the source code as a zip file and extract it.
   
   `git clone https://github.com/LukaszSwor/Messenger.git`
3. Navigate to the project directory.

## Running the Application

### With IntelliJ IDEA

1. Open the project in IntelliJ IDEA.
2. Make sure the project is recognized as a Maven project and IntelliJ has indexed all the Maven dependencies.
3. Configure the Main class of the client and server as your run configurations.
4. Run the server application first, then the client application.

### From the Terminal

1. Open a terminal and navigate to the MessengerServer directory.
2. Run `mvn clean install` to build the server application.
3. Start the server with `mvn javafx:run`.
4. Open another terminal and navigate to the MessengerClient directory.
5. Run `mvn clean install` to build the client application.
6. Start the client with `mvn javafx:run`.

## Usage
1. When you launch the client, enter the IP address and port number of the server you wish to connect to (by default, it connects to localhost on port 1234).
2. Type your message into the text field and click "Send" or press Enter to send a message.
3. Click "Send File" to send a file. A file dialog will open, allowing you to choose the file to send.

## Contributing
If you would like to contribute to the development of Barabasz's Messenger, please follow these steps:

1. Fork the repository.
2. Create a new branch (git checkout -b feature/YourFeature).
3. Commit your changes (git commit -am 'Add some feature').
4. Push to the branch (git push origin feature/YourFeature).
5. Create a new Pull Request.

## Contact
Name: Lukasz Sworacki
Email: lukasz.m.sworacki@gmail.com
GitHub: https://github.com/LukaszSwor
Thank you for using Messenger!
