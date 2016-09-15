<body> 
    <h1>Brand-builder content importer</h1>
	<form id="importPackage" method="POST" action="/etc/imports" enctype="multipart/form-data">
		<input type="file" name="*" /> <input type="Submit" />
	</form>
    <div class="uploadedPackages">
        <ul>

        </ul>
    </div>
    <script type="text/javascript">
    $("#importPackage").submit(function(e){     
    //disable the default form submission
    e.preventDefault();
    //grab all form data  
     var formData = new FormData($(this)[0]);
  console.log(formData);

  var formDataSerialized = $(this).serialize();
  console.log(formDataSerialized);
    var formUrl = $(this).attr("action"); 
        $.ajax({
            url: formUrl,
            type: 'POST',
            data: formData,
            async: false,
            cache: false,
            contentType: false,
            processData: false, 
            success: function (data, textStatus, xhr) {
                if(xhr.status == 200){
                var pathname = window.location.pathname;
                var url = pathname.replace('.html','.importlist.list');
                 $.ajax({
			         type: "GET",
			         url: url,

			         success:function(data) 
				      {
				        $(data).each(function(e) {
					    console.log(e);
					    });
				      },
				    error: function(jqXHR, textStatus, errorThrown) 
                        {

                        }
			       });
                }
            },
            error: function(){
                console.log("error in ajax form submission");
            }
        });

});
</script>   


</body>


