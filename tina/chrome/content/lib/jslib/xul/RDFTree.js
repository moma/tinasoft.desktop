/****************** Globals **********************/

const JSLIB_TREE                     = "tree.js";
const JSTREE_OK                      = true;
const JSLIB_TREE_SORT_PROGID         = '@mozilla.org/xul/xul-sort-service;1';

/****************** Globals **********************/


/****************** File Object Class *********************/

function RDFTree(aID) {
  if(aID)
    this.mID= aID;
} // constructor

RDFTree.prototype  = 
{
  mID           : null,
  mTree         : null,

  get tree()
    {
      if (this.mTree) return this.mTree;
      var tree = document.getElementById(this.mID);
      if(!tree) {
        throw("Unable to get tree.  ID: "+this.mID);
      }
      this.mTree = tree;
      return tree;
    },

  addDataSource :
    function(aRDF_URL_Path)
    {
      var tree = this.tree;

      var ds = Components.classes['@mozilla.org/rdf/rdf-service;1'].getService().QueryInterface(Components.interfaces.nsIRDFService).GetDataSource(aRDF_URL_Path);

      tree.database.AddDataSource(ds);
      tree.builder.rebuild();
    },
    
  removeDataSource :
    function(aRDF_URL_Path)
    {
      var tree = this.tree;

      var ds = Components.classes['@mozilla.org/rdf/rdf-service;1'].getService().QueryInterface(Components.interfaces.nsIRDFService).GetDataSource(aRDF_URL_Path);

      tree.database.RemoveDataSource(ds);
      tree.builder.rebuild();
    },
  
  focus :
    function()
    {
      this.tree.focus();
    },

  blur :
    function()
    {
      this.tree.blur();
    },

  rebuild :
    function()
    {
      this.tree.builder.rebuild();
    },

  get selected()
    {
      if (this.tree.treeBoxObject.selection == null) return -1;
      return (this.tree.treeBoxObject.selection.currentIndex);
    },

  get selectCount()
    {
      if (this.tree.treeBoxObject.selection == null) return 0;
      return (this.tree.treeBoxObject.selection.count);
    },

  set selected(index)
    {
      // Don't attempt to make any selections on an empty tree
      if (this.tree.treeBoxObject.view == null) return;
      
      this.tree.treeBoxObject.selection.select(index);

      // Give the tree focus (this will also allow the user to immediately
      // use the up/dopwn arrows -- bonus points!)
      this.tree.focus();
    },

  get count()
    {
      if (this.tree.treeBoxObject.view == null) return 0;
      return (this.tree.treeBoxObject.view.rowCount);
    },

  get treeitems()
    {
      return (this.tree.treeBoxObject.view);
    },

  get selectedID()
    {
// dump("--[ selectedID ]-- selectCount="+ this.selectCount +"   selected="+ this.selected +"\n");
      if (this.selectCount == 0 || this.selected == -1) return null;
      var element = this.tree.contentView.getItemAtIndex( this.selected );
      return element.getAttribute('id');
    },

  get selection()
    {
      // NOTES:
      // (1) This returns an nsITreeSelection element.  Use getSelectedIDs()
      //     to get a list of IDs.
      return this.tree.treeBoxObject.selection;
    },
  
  get getSelectedIDs()
    {
      var selectionArray = new Array();

      // Get the current selection
      var selection = this.selection;
      var rangeCount = selection.getRangeCount();

      // The rangeCount represents the number of different groups of selections
      // (i.e. there may be gaps in the selection)
      for (var range = 0; range < rangeCount; ++range)
      {
        var min = {}, max = {};
        selection.getRangeAt(range, min, max);
        for (var index = min.value; index <= max.value; ++index) {
          var item = this.tree.contentView.getItemAtIndex(index);
          selectionArray.push( item.getAttribute('id') );
        }
      }

      return selectionArray;
    },

  clearSelection :
    function(index)
    {
      if (index != null) {
        this.tree.treeBoxObject.selection.clearRange( index,index );
      }
      else {
        this.tree.treeBoxObject.selection.clearSelection();
      }
    },

  getRowIndexOf :
    function(which)
    {
      var element = document.getElementById(which);
      var index = this.tree.contentView.getIndexOfItem( element );
      return index;
    },

  //------------------------------------------------------------------------
  // Column sort utilities
  //------------------------------------------------------------------------
  doSort :
    function(column)
    {
      var tree = this.tree;
      var node = document.getElementById(column);
      if (!node) return false;

      var sortKey = node.getAttribute('resource');
      if (!sortKey) return false;

      var sortDirection = "ascending";
      if ( node.getAttribute('sortDirection') == "ascending" )
        sortDirection = "descending";
      else
        sortDirection = "ascending";

      this.updateSortIndicator(column, sortDirection);
      this.sort(column, sortKey, sortDirection);

      // Save the current sort settings for use by SortToPreviousSettings()
      tree.setAttribute('sortColumn',column);
      tree.setAttribute('sortKey',sortKey);
      tree.setAttribute('sortDirection',sortDirection);
      node.setAttribute('sortDirection',sortDirection);

      return true;
    },

  //------------------------------------------------------------------------
  refreshSort :
    function()
    {
      // Retrieve the last sorted state from the tree element
      var tree = this.tree;
      var column = tree.getAttribute('sortColumn');
      var node = document.getElementById(column);
      var sortKey = node.getAttribute('resource');
      var sortDirection = node.getAttribute('sortDirection');

      this.sort(column, sortKey, sortDirection);

      // Force the button states to be updated by giving the tree focus
      // (this will also allow the user to immediately use the up/down
      // arrows --- bonus points!)
      tree.focus();
    },

  //------------------------------------------------------------------------
  sortColumn :
    function(column, sortKey, direction)
    {
      var tree = this.tree;
      var node = document.getElementById(column);
      if (!node) return false;

      // If a specific direction passed in as a parameter, use it; otherwise
      // get the last sort direction from the tree node (defaulting to
      // "ascending" if none available)
      var sortDirection;
      if (direction != null) {
        sortDirection = direction;
      }
      else {
        sortDirection = "ascending";
        if ( node.getAttribute('sortDirection') == "ascending" )
          sortDirection = "descending";
        else
          sortDirection = "ascending";
      }

      this.updateSortIndicator(column, sortDirection);
      this.sort(column, sortKey, sortDirection);


      // Save the current sort settings for use by SortToPreviousSettings()
      tree.setAttribute('sortColumn',column);
      tree.setAttribute('sortKey',sortKey);
      tree.setAttribute('sortDirection',sortDirection);
      node.setAttribute('sortDirection',sortDirection);

      this.rebuild();

      return true;
    },

  //------------------------------------------------------------------------
  sort :
    function(column, key, direction)
    {
      var xulSortService = Components.classes[JSLIB_TREE_SORT_PROGID].getService();
      xulSortService = xulSortService.QueryInterface(Components.interfaces.nsIXULSortService);
      if (!xulSortService) return (false);

      var node = document.getElementById(column);
      if ( node )
      {
        try {
          xulSortService.Sort(node, key, direction);
        }
        catch (ex) {}
      }
    },

  //------------------------------------------------------------------------
  sortToPreviousSettings :
    function()
    {
      var tree = this.tree;

      // Retrieve the last sorted state from the tree element
      var column = tree.getAttribute('sortColumn');	
      var sortKey = tree.getAttribute('sortKey');
      var sortDirection = tree.getAttribute('sortDirection');

      // Provide default sort data if none was d			
      if ( !column || !sortKey )
      {
        return;
      }
      if ( !sortDirection )
        sortDirection = 'ascending';

      this.updateSortIndicator(column,sortDirection);
      this.sort(column, sortKey, sortDirection);
    },

  //------------------------------------------------------------------------
  // Sets the column header sort icon based on the requested 
  // column and direction.
  // 
  // Notes:
  // (1) This function relies on the first part of the 
  //     <treecell id> matching the <treecol id>.  The treecell
  //     id must have a "Header" suffix.
  // (2) By changing the "sortDirection" attribute, a different
  //     CSS style will be used, thus changing the icon based on
  //     the "sortDirection" parameter.
  //------------------------------------------------------------------------
  updateSortIndicator :
    function(column,sortDirection)
    {
      // set the sort indicator on the column we are sorted by
      if (column)
      {
        var sortedColumn = document.getElementById(column);
        if (sortedColumn)
        {
          sortedColumn.setAttribute("sortDirection",sortDirection);

          // remove the sort indicator from all the columns
          // except the one we are sorted by
          var currCol = sortedColumn.parentNode.firstChild;
          while (currCol) {
            if (currCol != sortedColumn && currCol.localName == "treecol") {
              currCol.removeAttribute("sortDirection");
            }
            currCol = currCol.nextSibling;
          }
        }
      }

    }
};

