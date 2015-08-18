var DepartmentMgrTPage = {

  jsLoaded : function() {
    var doToggle = this._toggle;

    $$('#idDepartmentMgrTPage_tbl img.toggle').each(function(img) {
      img.observe("click", function() {
        doToggle(img, img._open, true);
      });

      var cookiek = "toggle_" + img.up(".titem").getAttribute("rowid");
      doToggle(img, document.getCookie(cookiek) == "true");
    });
    
    var pa = $Actions['DepartmentMgrTPage_tbl'];
    pa.move = function(up, moveAction, o) {
      var row = pa.row(o);
      var cls = "div[parentid=" + row.getAttribute("parentid") + "]";
      var row2 = up ? row.previous(cls) : row.next(cls);
      if (!row2) {
        alert($MessageConst["Error.Move"]);
        return;
      }
      pa._moveAction(moveAction, [ pa.rowId(row), pa.rowId(row2) ]);
    };
    pa.move2 = function(up, moveAction, o) {      
      var row = pa.row(o);
      var cls = "div[parentid=" + row.getAttribute("parentid") + "]";
      var rowIds = [];
      while (row) {
        rowIds.push(pa.rowId(row));
        row = up ? row.previous(cls) : row.next(cls);
      }
      if (rowIds.length < 2) {
        alert($MessageConst["Error.Move"]);
        return;
      }
      pa._moveAction(moveAction, rowIds);
    };
  },

  toggleAll : function(open) {
    var doToggle = this._toggle;

    $$('#idDepartmentMgrTPage_tbl img.toggle').each(function(img) {
      doToggle(img, open, true);
    });
  },

  _toggle : function(img, open, cookie) {
    var src = img.src;
    var p = src.lastIndexOf("/") + 1;
    var path = src.substring(0, p);

    var target = function() {
      var arr = [];
      var item = img.up(".titem");
      var parent = item.getAttribute("parentid");
      var nitem = item.next();
      while (nitem) {
        var parent2 = nitem.getAttribute("parentid");
        if (parent == parent2) {
          break;
        }
        arr.push(nitem);
        nitem = nitem.next();
      }
      return $(arr);
    };

    if (open) {
      target().invoke("show");
      img.src = path + "p_toggle.png";
    } else {
      target().invoke("hide");
      img.src = path + "toggle.png";
    }
    img._open = !open;
    if (cookie) {
      var cookiek = "toggle_" + img.up(".titem").getAttribute("rowid");
      document.setCookie(cookiek, open);
    }
  }
};
