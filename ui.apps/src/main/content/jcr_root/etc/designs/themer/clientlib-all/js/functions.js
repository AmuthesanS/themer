$.get('/etc/brandbuilder-importer.package.list', {
	type : "imports"
}).success(function(data) {
	var template = '<li><a class="import-package" data-path=<%=path%> href="#"><%=name%></a></li>';
	_.each(data, function(element) {
		var output = _.template(template);
		var outputString = output(element);
		 $('#package-list').append(outputString);
	});
    $('.import-package').click(function(){
        var zipPath = $(this).data('path');
        $.get('/etc/brandbuilder-importer.import.html',{zipPath :zipPath}).success(function(){
			console.log('import job added!!!');
        });
    });
});