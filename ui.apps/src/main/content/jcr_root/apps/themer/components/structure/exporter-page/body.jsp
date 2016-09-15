<body>
	<h1>Brand-builder content exporter</h1>
	<form id="ajaxform">
		<input name="page" value="" type="text" id="pageURL" /> <input
			type="submit" value="generate" />
	</form>
	<a id="download-link"><button>Download</button></a>
	<div id="package-list"></div>
	<form id="fileCompare">
		File1: <input name="file1" value="" type="text" id="file1" />
		File2: <input name="file2" value="" type="text" id="file2" />
		<input type="submit" value="compare" />
	</form>
	<div id="file-compare-results"></div>
	<style>
		table, td {
			border: 1px solid black;
			padding: 10px;
		}
	</style>
	<script type="text/javascript">
		$("#ajaxform").submit(function(e) {
			e.preventDefault();
			var formURL = $("#pageURL").val() + ".theming.zip";
			$.ajax({
				type : "GET",
				url : formURL,
				success : function(data) {
					$("#download-link").attr("href",data);
					alert("Package generated succesfully");
				},
				error : function(jqXHR, textStatus, errorThrown) {
					alert("Failed to generate package");
				}
			});
		});
		$("#fileCompare").submit(function(e) {
			e.preventDefault();
			/* Comparing two files */
			var file1 = $("#file1").val();
			var file2 = $("#file2").val();
			$.ajax({
				type : "GET",
				url : "/etc/brandbuilder-exporter.file.comp",
				data : {'file1':file1,'file2':file2},
				success : function(data) {
					var obj = data;
                    var deleteTemplate='<td>{{linenum}}</td><td>{{leftfile}}</td><td>{{rightfile}}</td>';
                    $.each(obj, function(key,value) {	
                    $("#file-compare-results").empty();
                    $("#file-compare-results").append("<tr>");
                    $("#file-compare-results").append(Mustache.to_html(deleteTemplate, value));
                    $("#file-compare-results").append("</tr>");
                    });
				},
				error : function(jqXHR, textStatus, errorThrown) {
					//if fails		
				}
			});
		});
		$(document).ready(function(e) {
			$.ajax({
				type : "GET",
				url : "/etc/brandbuilder-exporter.package.list",
				success : function(data) {
                    var obj = data;
                    var deleteTemplate='<td><a href={{path}} target="_blank">{{name}}</a></td><td><form method="POST" action="/etc/exports" enctype="multipart/form-data"><input name=":redirect" type="hidden" value="/etc/brandbuilder-exporter.html"/><input type="hidden" name="{{path}}@Delete"/><input type="Submit" value="Delete"/></form></td>';
                    var counter = 0;
						$.each(obj, function(key,value) {
                            counter++;
							$("#package-list").append("<tr>");
                            $("#package-list").append("<td>"+counter+"</td>");
                            $("#package-list").append(Mustache.to_html(deleteTemplate, value));
                            $("#package-list").append("</tr>");
					}); 
				},
				error : function(jqXHR, textStatus, errorThrown) {
					//if fails		
				}
			});
		});

	</script>
</body>