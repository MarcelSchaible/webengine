<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>WebEngine Search Page</title>



<link rel="stylesheet" href="css/bootstrap.min.css">

<link href="js/barIndicator/css/bi-style.css" rel="stylesheet" />
 <script src="js/jquery-1.11.2.min.js"></script>
  <script src="js/jquery.easing.1.3.js"></script>
 <script src="js/barIndicator/jquery-barIndicator.js"></script>

<script>
$(document).ready(function(){
	
	var quality = <c:out value="${quality }" />;
	
	var opt = {
			 
			 horLabelPos:'left',
			 
				 colorRange:true,
				 colorRangeLimits: {
				  optimal: '75-100',	 
				  newRangeThree: '55-74-rgb(241,144,40)',
				  critical: '0-54'
				 }
 
			};
	
	if (quality >=0 && quality<=100) {
		
	$('#mydiv').text("Query Quality:");	
	$('#mydiv').show();
		
	$('#mydiv2').show();	
	$('#bar').barIndicator(opt);
	
	$('#bar').show();
	
	} else {
		
		$('#mydiv').hide();
		$('#mydiv2').hide();
		$('#mydiv2').text("Currently not available.");
		$('#mydiv2').css("padding-top", "4px");
		
		
	}
	
	
	var centroid = "<c:out value="${centroid}" />";
	
	if ((centroid!="") && centroid.length>1)  {
		
		$('#mydiv3').text("Query Centroid:");	
		$('#mydiv3').show();
		
		//$('#mydiv4').text(centroid);	
		$('#mydiv4').show();
		
	} else {
		
		$('#mydiv3').hide();
		$('#mydiv4').hide();
		
		
	}
	
	
	
	$('.local').each(function(i, obj) {
    //test
	
	 var help = $(obj).attr("href");
	 var help2 = window.location.hostname+"";
	
	
	 
	 if (help.indexOf(help2)==-1) {
		 
		 
		 if (help.indexOf("/",9)!=-1)  {
			 
			 var first =  help.substring(0, help.indexOf("/",9));
			// alert(first);
			 $(obj).attr("href", first);
			 
			 
		 } else {
			// alert("2");
			 $(obj).attr("href", help);
			 
		 }
		 
		 
		 $(obj).show();
	 }
	 
	
	});
	
	
	
	
	
	
});



function setCookie(cname,cvalue,exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires=" + d.toGMTString();
    document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}

function getCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for(var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function checkCookie() {
	
    var ft=getCookie("ftresults");
    if (ft != "") {
        //alert("Welcome again " + user);
		if (ft=="yes") {
			
			document.getElementById("ftCheck").checked = true;

			var text = document.getElementById("ftresults");
			
				text.style.display = "block";
			
			
			
			
		} else {
			
			
			document.getElementById("ftCheck").checked = false;

			var text = document.getElementById("ftresults");
			
				text.style.display = "none";
			
			
		}
			
		
    } else {
       var help="yes";
           setCookie("ftresults", help, 30);
		   
		   
		   document.getElementById("ftCheck").checked = true;

			var text = document.getElementById("ftresults");
			
				text.style.display = "block";
       
    }
	
	
	
	
	
	
	
	
	   var cd=getCookie("cdresults");
    if (cd != "") {
        //alert("Welcome again " + user);
		if (cd=="yes") {
			
			document.getElementById("cdCheck").checked = true;

			var text = document.getElementById("cdresults");
			
				text.style.display = "block";
			
			
			
			
		} else {
			
			
			document.getElementById("cdCheck").checked = false;

			var text = document.getElementById("cdresults");
			
				text.style.display = "none";
			
			
		}
			
		
    } else {
       var help="yes";
           setCookie("cdresults", help, 30);
		   
		   
		   document.getElementById("cdCheck").checked = true;

			var text = document.getElementById("cdresults");
			
				text.style.display = "block";
       
    }
	

	
}


function myFunction() {
	
    var checkBox = document.getElementById("ftCheck");
    var text = document.getElementById("ftresults");
    if (checkBox.checked == true){
        text.style.display = "block";
		setCookie("ftresults", "yes", 30);
    } else {
       text.style.display = "none";
	   setCookie("ftresults", "no", 30);
    }
	
	
	
	var checkBox = document.getElementById("cdCheck");
    var text = document.getElementById("cdresults");
    if (checkBox.checked == true){
        text.style.display = "block";
		setCookie("cdresults", "yes", 30);
    } else {
       text.style.display = "none";
	   setCookie("cdresults", "no", 30);
    }
	
	
	
}




   

</script>



</head>
<body onload="checkCookie()">
		
	<div class="container">
		<img src="css/cooltext296417035504939.png" alt="WebEngine" style="width:100%">
		
		<div class="header clearfix"></div>
		
		<div class="jumbotron" style="padding-bottom:45px;margin-bottom:15px">
			  
		
	
			<p class="lead" style="margin-top:-30px">The first fully integrated, decentralised web search engine.</p>
			<div style="clear:both"></div>
			
			<div id="wrapper1" style="width:70%;margin-top:15px">
			<form action="search.html" accept-charset="UTF-8">
				<div class="input-group" >
					<input type="text" class="form-control" name="searchParam"
						value="<c:out value="${searchString }"/>"
						placeholder="Search for..."> <span class="input-group-btn">
						<button class="btn btn-default" type="submit">Go!</button>
					</span>
				</div>
			</form>
			</div>
			
			<div id="wrapper" style="height:15px; width:510px; margin-top:10px">

				<div id="mydiv" hidden style="height:15px; width:105px; float:left; padding-top:4px">
				
				</div>
				<div id="mydiv2" hidden style="height:15px; width:403px; float:right">
				<span id="bar" hidden ><c:out value="${quality }" /></span>
				</div>
				
				
				<div id="mydiv3"  style="height:15px; width:107px; float:left; padding-top:15px">
				
				</div>
				<div id="mydiv4"  style="height:15px; width:398px; float:right; padding-top:15px; ">
				<a href='search.html?searchParam=<c:out value="${centroid }" />'><c:out value="${centroid }" /></a>
				</div>
				
				
			</div>
		</div>

		
		<div class="jumbotron" style="padding-bottom:0px;padding-top:20px;">	
		<p style="margin-bottom:5px;">Centroid Results <input type="checkbox" id="cdCheck" onclick="myFunction()"> :</p>
			<div id="cdresults" style="display:none;">
			<c:forEach items="${results}" var="result">
				<c:if test="${result.centroidsearch eq true}">
					<div class="col-lg-12" style="max-height:100px;">
						<h4 style="margin-bottom: 3px;"  >
							<a href="<c:out value="${result.href }"/>" style="word-wrap: break-word;" ><c:out
									value="${result.title }" /></a>	
							
						</h4>
						
						
						
						<h6  style="margin-top: 5px;">
						<a class="local" href="<c:out value="${result.href }"/>" onclick="changeHref(this, '<c:out value="${result.href }" />'  )" style="cursor: pointer;display:none;" >Change to this WebEngine...</a>
						</h6>
						
						
						
					</div>
				</c:if>
			</c:forEach>
			
			</div>
			
		
		&nbsp;
		<p style="margin-bottom:5px;margin-top:-10px;">Full-text Results <input type="checkbox" id="ftCheck" onclick="myFunction()"> :</p>
			<div id="ftresults" style="display:none;">
			<c:forEach items="${results}" var="result">
				<c:if test="${not result.centroidsearch }">
					<div class="col-lg-12" style="max-height:100px;">
						<h4 style="margin-bottom: 3px;"  >
							<a href="<c:out value="${result.href }"/>" style="word-wrap: break-word;" ><c:out
									value="${result.title }" /></a>	
							
						</h4>
						<h6  style="margin-top: 5px;" >
						<a class="local" href="<c:out value="${result.href }"/>" onclick="changeHref(this, '<c:out
									value="${result.href }" />'  )" style="cursor: pointer;display:none;" >Change to this WebEngine...</a>
						</h6>
						
					</div>
				</c:if>
			</c:forEach>
			</div>

		
		
			
					&nbsp;

		
		</div>
			<footer class="footer" style="text-align: center; padding-bottom:10px;">		
Copyright &copy; 2019 by <a href="https://www.fernuni-hagen.de/kn/" target="_blank">Chair of Communication Networks</a>, University of Hagen, Germany			
			</footer>
		
		
		<!-- /container -->
</div>


	<script>
	

</script>

		<!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
		<script src="js/ie10-viewport-bug-workaround.js"></script>
</body>
</html>