# **MagicShare(English)**

[**中文说明**](./README.md)

---

Website:

[MagicShare_EN | ZZHow](https://www.zzhow.com/en/MagicShareEN)

Source Code:

https://github.com/ZZHow1024/MagicShare

Releases:

https://github.com/ZZHow1024/MagicShare/releases

---

## What is it?

MagicShare is a cross-platform intranet file sharing tool.

---

## Technical route

![TechnicalRoute.png](./TechnicalRoute.png)

- programming language: **Java** and **JavaScript**

---

## **License**

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](https://github.com/ZZHow1024/MagicShare/blob/main/LICENSE) file for details.

---

## User License Agreement

**Please read carefully before using this software:**

- Legal use: This software is limited to legal file sharing. It is strictly forbidden to share any files that infringe copyright, involve pornography, violence, fraud, illegal or other harmful content.
- Personal responsibility: You are fully responsible for the legality of the shared content. Please make sure that you have the legal authorization to share the file.
- Risk warning: This software cannot guarantee the security of the shared files. Please check the security of the files yourself.
- Disclaimer: The software author is not responsible for any direct or indirect losses caused by the use of this software.

The use of MagicShare requires agreement and compliance with the above.

---

## **Instructions for use**

Download address:

https://github.com/ZZHow1024/MagicShare/releases

### Desktop Client

- Determine the operating system you are using.
    - Linux：
        - Select the .deb installation package (Debian, Ubuntu) / .rpm (Red Hat, Fedora, SUSE) installation package.
    - macOS：
        - Determine the chip of the Mac you are using (Apple Silicon / Intel).
        - Select the .dmg disk image / .pkg installation package.
    - Windows：
        - Select the .zip compressed package / .exe installation package / .msi installation package.
    - General:
        - Select the .jar package (the computer needs to have JRE configured)
- Download the corresponding file.
- Linux and macOS need to be installed before running. Windows can directly run the .exe executable program in the .zip compressed package or select the .exe installation package and .msi installation package to perform the installation operation. The .jar package can be directly run through the `java -jar` command.
- Launch MagicShare and read the instructions on the startup page carefully. You can continue to use it after agreeing to the "User License Agreement".
- You can select the language at the bottom right of the main interface of MagicShare.
    - Currently supports Chinese (Simplified / Traditional) and English.
- Choose whether to enable the connection password
    - If you want to enable the connection password, you need to check "Enable password" and customize a 3-10-digit password.
    - If you do not want to enable the connection password, you need to uncheck "Enable password".
- After customizing the port, click the "Start Service" button
    - If the prompt "Start Success" is displayed, it means that the service is started normally and the "Share URL" can be provided to the recipient.
    - If the prompt "Port is occupied" is displayed, try to change the port number.
    - If the prompt "Wrong port number", please check whether the customized port number is an integer between 1 and 65535.
- Add the files to be shared to the sharing list
    - Method 1: Drag the file/folder to be shared to the upper half of the software main interface.
    - Method 2: Click the "Select folder" button to select the folder to be shared.
    - Method 3: Enter the path of the file/folder to be shared in the "Shared file/folder" text input box and press the "Enter" key.
- You can check the current number of connections in the upper right corner of the software.
- Press the "Stop Service" button to terminate sharing immediately.
- Press the "Clear sharing list" button to clear the share list immediately.

### Web client

- Open the browser and visit "Share URL".
- Launch MagicShare and read the instructions on the startup page carefully. Agree to the "User License Agreement" to continue using it.
- You can select the language in the lower right corner of the MagicShare main interface.
    - Currently supports Chinese (Simplified/Traditional) and English.
- Download files
    - Click "Quick download" to use the browser downloader to quickly download files via HTTP protocol.
    - Click "Encrypted download" to use MagicShare encrypted downloader to download files via WebSocket protocol and use RSA+AES hybrid encryption. It does not support simultaneous encrypted download of multiple files.
- Click the "Decryption download progress" button, and the encrypted download progress drawer will pop up at the bottom of the page.

---

## **Dependencies**

This project requires the following libraries:

- [**Vue.js**](https://github.com/vuejs) and supporting components: used to build Web frontend programs.
- [**Spring Boot**](https://github.com/spring-projects/spring-boot) and supporting components: used to build Web backend programs.
- [**OpenJFX**](https://openjfx.io/): JavaFX library for building graphical user interfaces.

---

## **Functional introduction of each version**

- MagicShare1.0.0
    - Start service on custom port.
    - Find files by folder/file path and generate a list.
    - Download files from Web page.
        - Support fast file download via HTTP protocol
        - Support downloading files via WebSocket protocol and using RSA+AES hybrid encryption
- MagicShare2.0.0
    - Display the current number of connections.
    - Customize the connection password.
    - Support multiple languages.
        - Chinese (Simplified/Traditional)
        - English

---

## **Main interface of each version**

### MagicShare2.0.0

![MagicShare2.0.0-Desktop-EN](https://lively-brook-dc1.notion.site/image/attachment%3Af185d39b-6a7c-4334-b51a-6821c72586c5%3AMagicShare2.0.0-Desktop-EN.png?table=block&id=760a4fa4-e27f-49a6-afd8-9fa26d5bf88b&spaceId=4b165318-6383-451c-8845-110b786c9f0a&width=1420&userId=&cache=v2)

MagicShare2.0.0-Desktop-EN

![MagicShare2.0.0-Web-EN](https://lively-brook-dc1.notion.site/image/attachment%3A7f1c2b9b-6013-4bf7-bab7-4ea71051fcb0%3AMagicShare2.0.0-Web-EN.png?table=block&id=b25b96d5-1040-4164-abd0-45f0c69b6c7a&spaceId=4b165318-6383-451c-8845-110b786c9f0a&width=1420&userId=&cache=v2)

MagicShare2.0.0-Web-EN

### MagicShare1.0.0

![MagicShare1.0.0-Desktop](https://lively-brook-dc1.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F4b165318-6383-451c-8845-110b786c9f0a%2Fcc029b9b-e911-4cd4-b9c7-4b0853fa6d04%2FMagicShare1.0.0-Desktop.png?table=block&id=194e64bd-e40f-8068-a52e-efc6ba94c62e&spaceId=4b165318-6383-451c-8845-110b786c9f0a&width=1420&userId=&cache=v2)

MagicShare1.0.0-Desktop

![MagicShare1.0.0-Web](https://lively-brook-dc1.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F4b165318-6383-451c-8845-110b786c9f0a%2Fa457bfec-f826-4936-8721-75fc46632fc2%2FMagicShare1.0.0-Web.png?table=block&id=194e64bd-e40f-80d1-957a-c58c4ea64b02&spaceId=4b165318-6383-451c-8845-110b786c9f0a&width=1420&userId=&cache=v2)

MagicShare1.0.0-Web
