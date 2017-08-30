val myData = "16 3 -2 8 -4 5"
val count=2

myData.split("\\D+").map(_.toInt).min
myData.split(" ").map(_.toInt).minBy(t=>(t.abs,t<0))