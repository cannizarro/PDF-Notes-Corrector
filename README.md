# PDF-Notes-Corrector

The app is finally done with the main app source code in [PDF_Corrector](https://github.com/cannizarro/PDF-Notes-Corrector/tree/master/PDF_Corrector) and the prototype code in [this](https://github.com/cannizarro/PDF-Notes-Corrector/tree/master/Prototypes). (Dated : 11/05/2019)

This is a personal project born out of my own needs. The app's them is sot

Remember every time when in your chat group someone sends a camera scanned PDF of some notes or documents. The PDF is generally taken in
a hurry and many a times some pages happen to be rotated 90*n degrees (clock or anti clock) and then it becomes a pain to read.

To scan and manipulate images we already have an awesome app (CamScanner) but if we just have the PDF there is no mainstream big name app.
So I created this app to hopefully fill this hole and just to add some more app building experience in my sad life.

You can also save individual images inside the PDF to your device's internal storage in JPEG form.

## Getting Started

The main apk file is stored in the repository and can be downloaded there or by [clicking here](https://github.com/cannizarro/PDF-Notes-Corrector/raw/master/PDF%20Corrector.apk).
The two prototypes with different visual aspects have been made. The `.apk` files are stored in the 
`Prototype APKs` folder and you are welcome to try them. You just need to download them. Believe me the real app will be far more visually appealing than this.


### Prerequisites

Minimum android SDK supported is 14 so anyone with Android 4.0 and above is fine and if you're not go see a doctor.
You should also enable `Install from unknown sources` in your settings, if you haven't already soy boy.

## How to use

#Final App :
  1. Choose PDF by clicking on the add button labelled as Add PDF.
  2. Then choose PDF from your device or external storage.
  3. Select any image by long pressing and then further images can be selected by short clicks.
  4. Now you can perform rotate left or right, select all, save or delete selected images or create PDF with the newly rotated images.
  5.The PDF and images will then be stored in (this is important) : **In a folder named after the PDF you selected inside another folder named PDF inside your device's download folder**
  
#Prototype :
  1. Choose PDF by clicking on the choose PDF button.
  2. Then choose PDF from your device or external storage.
  3. Select any image by long pressing and then further images can be selected by short clicks.
  4. Now you can perform rotate left or right, save selected images or create PDF with the newly rotated images.
  5.The PDF and images will then be stored in (this is important) : **In a folder named after the PDF you selected inside another folder named PDF inside your device's download folder**
 

## Built With

* [Apache PDFBox](https://pdfbox.apache.org/) - PDF manipulation library.
* [PdfBox-Android](https://github.com/TomRoush/PdfBox-Android) - Dependency used for running PDFBox on android.
* [Glide](https://github.com/bumptech/glide) - Library used for bitmap loading

## Contributing

Feel free to contribute.

## Authors

* **Asrar Ul Haque** - *Initial work* - [Cannizarro](https://github.com/cannizarro)


## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Hat tip to anyone whose code was used
* Inspiration
* etc
