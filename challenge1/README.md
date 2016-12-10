### Challenge 1: Online Ticker

Develop a function that transforms provided observable in such way so that subscription will occur only when device has Internet connection.
If `java.io.IOException` occurred in the stream (let's treat it as there is no Internet connection), try again with the same requirements.
To check connectivity status use `android.net.ConnectivityManager`.

Test the function on JVM (use [Spek](https://github.com/JetBrains/spek) for a bonus).

Present a demo stand where is an UI element that indicates connectivity status and an UI element to see a data from the stream.
Let's source to be some kind of infinity number emitter, for example `Observable.interval(1, TimeUnit.SECONDS)`
Reviewer will manupulate connectivity status using airplane mode. Throw `IOException` when status is "no internet" for demonstration purpose.