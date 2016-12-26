$ready(function() {
  var img = $('idPhotoFormPage_img');

  window.PhotoTPage = {
    create_cropper : function() {
      this._cropper = new Cropper(img, {
        dragMode : 'move',
        viewMode : 1,
        aspectRatio : 1 / 1,
        zoomable : false
      });
    },

    photo_clip : function() {
      if (!this._cropper) {
        this.create_cropper();
      }
      this._cropper.replace(img.src);
    },

    photo_set : function(url) {
      img.src = url.addParameter('d=' + new Date().getTime());
      this.photo_clip();
    },

    photo_save : function() {
      $Actions['PhotoFormPage_save']('data='
          + (this._cropper ? JSON.stringify(this._cropper.getData()) : ""));
    }
  };
});