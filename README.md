# Hackathon-Mindspark
Problem Statement: 
Paper leaking scandals are on the rise. Currently, Institutes use very primitive measures to handle these malpractices, 
which include presence of people. However, in the era of technology, this field has a very backward approach. 
Provide a solution where-in this problem can be tackled (You may use some points such as QR code based encoding, mobile monitoring and verification system) 
Note that the solution needs to be practical and easy to implement on a national scale
Solution: 
Android app soultion for paper leak scandals problem statement in <a href="http://www.coep.org.in/">COEP</a> 
<a href="http://www.mind-spark.org/hackathon.html">Mindspark Hackathon2.0</a>.
The proposed solution is to develop a new file format which provides three layer security which includes <a href="https://firebase.google.com/docs/auth/">Google Authenticaton</a>,
password protection and device <a href="https://en.wikipedia.org/wiki/MAC_address">MAC address</a> verfication using mac addresses stored in file.
The file uses encryption provided by <a href="https://github.com/facebook/conceal">Facebook Conceal</a>. The file is uploaded to Google Cloud 15 minutes before exams and downloaded by respective
institutes by providing required credentials. The paper can be either printed directly by connecting to printer or saving file as pdf and then printing.
