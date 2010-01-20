
function walk_rdf(file) {
   var rdf = new RDFFile(file);
   dump("Source: " + rdf.getSource() + "\n");
   var conts = rdf.getAllContainers();
   for(var i=0; i<conts.length; i++) {
      walk_container(conts[i]);
   }
}

function walk_container(res) {
   var list;
   dump("Container: " + res.getSubject() + "\n");
//   walk_attributes(res);
   list = res.getSubContainers();
   for(var i=0; i<list.length; i++) {
      walk_container(list[i]);
   }
   list = res.getSubNodes();
   for(var i=0; i<list.length; i++) {
      dump("\tnode: " + list[i].getSubject() + "\n");
      walk_attributes(list[i]);
   }
}

function walk_attributes(node) {
   list = node.getAllAttributes();
   for(var i=0; i<list.length; i++) {
      dump("\t\tattr: [name=" + list[i].name + "][value=" + list[i].value + "]\n");
   }
}
