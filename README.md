# MacIndexer Android application and Specs database

Designed for quick vintage Macintosh computer specifications look-up on Android Devices, to serve the community.

## Acknowledgment

This is part of University of Illinois, CS125 FA19 Final Project. This draft app is for education purpose, and internal use only. It won't release to public. It uses information directly from Mactracker. Data it provides are also limited, please refer to the "Database Design Information" section below.

## Credits

## Database Design Information

This section is provided for people who want to maintain the database or application.

### Categories (tables)
- category0: Classic Macintosh.
- category1: eMac.
- category2: iMac.
- category3: Mac mini.
- category4: Mac Pro.
- category5: Performa.
- category6: Power Macintosh.
- category7: Power Mac G3/G4/G5.
- category8: iBook.
- category9: PowerBook.
- category10: PowerBook G3/G4.

(From very first Macintosh to last PowerPC-based Mac models.)

Class "CategoryHelper" is designed to handle these categories.

### Data Categories (columns)

- id: Internal ID for sorting and debugging. Won't present in application interface.
- name: Machine Name.
- processor: Processor Type.
- maxram: Maximum RAM.
- year: Beginning Year of Production.
- model: Model Number.
