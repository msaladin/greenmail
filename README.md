GreenMail (AdNovum fork)
=======================

[GreenMail][greenmail_project_site] is an open source, intuitive and easy-to-use test suite of email servers for testing purposes. 
Supports SMTP, POP3, IMAP with SSL socket support. GreenMail also provides a JBoss GreenMail Service.
GreenMail is the first and only library that offers a test framework for both receiving and retrieving emails from Java.

This fork contains two additional features that was not yet accepted from the official master of the Greenmail project:
* Feature: Mail sink. Ability to configure that all mails are destined to a single mailbox.
* Feature: File mailstore for persistent message-storage (configurable, default store is the original in-memory store.

For general information about Greenmail, please see the main project site:

Go to the [project site][greenmail_project_site] for details:

Feature details
===============

How to configure a mailsink?

How to configure the file mailstore?


Deployment details
==================
CircleCI builds it, so we have this manual release process:
1) Change the versions in all pom.xml
2) Commit and Push
3) Wait until CircleCI can be accessed using SSH (https://circleci.com/docs/ssh-build)
4) Use ssh and scp to copy all JARs/Wars, e.g. with a proxy like this:
    ssh -p 64656 ubuntu@52.14.4.86 -o "ProxyCommand=nc -X connect -x proxy.adnovum.ch:3128 %h %p"
    scp -P 64656 -o "ProxyCommand=nc -X connect -x proxy.adnovum.ch:3128 %h %p" ubuntu@52.14.4.86:/home/ubuntu/greenmail/./greenmail-core/target/greenmail-1.6.0-SNAPSHOT.jar .
5) Do some manual testing
6) Upload to github
7) Do some company related stuff
7a) Upload to Nexus
7b_)e.g. build the adngreenmail ReleaseBuild with the new version

