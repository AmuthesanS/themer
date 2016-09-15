<body>
	<h1>Brand-builder content exporter</h1>
	<form id="ajaxform">
		<input name="page" value="" type="text" id="pageURL"/> <input type="submit" value="generate" />
	</form>
	<a id="download-link"><button>Download</button></a>
	
	<div id="PackagesList"></div>
	<script type="text/javascript">
	var formURL = $("#pageURL").val();
	
	$("#ajaxform").submit(function(e){	
		e.preventDefault();
	$.ajax({
		  type: "GET",
		  url: formURL+".theming.zip",
		  success:function(data) 
			{
				alert("Zip generated successfully")
			},
			error: function(jqXHR, textStatus, errorThrown) 
			{
				alert("An error occurred");		
			}
		});
	});
	
	$(function(){
		$.ajax({
			  type: "GET",
			  url: formURL+".package.list",
			  success:function(data) 
				{
				  $(data).each(function(e) {
					 console.log(e);
					});
				},
				error: function(jqXHR, textStatus, errorThrown) 
				{
					//if fails		
				}
			});
	});
	
	
	</script>
</body>

