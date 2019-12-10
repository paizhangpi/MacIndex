# MacIndex

Designed for quick vintage Macintosh computer specifications look-up on Android Devices.

## Acknowledgment

This is part of University of Illinois, CS125 FA19 Final Project. This draft app is for education purpose, and internal use only. Photo credit to Apple.

## Credits

paizhangpi, Victor Jiao

## Database Design Information

This section is provided for people who want to maintain the database or application.

### Categories (tables)

- category0: Classic Macintosh.
- category1: eMac.
- category2: iMac.
- category3: Mac mini.
- category4: Performa.
- category5: Power Macintosh.
- category6: Power Mac G3/G4/G5.
- category7: iBook.
- category8: PowerBook.
- category9: PowerBook G3/G4.

(From very first Macintosh to last PowerPC-based Mac models.)

Note: Adding a new category in database

Please manually update CategoryHelper, initInterface and activity_main String resources and Readme.

### Data Categories (columns)

- id: Internal ID for sorting and debugging. Won't present in application interface.
- name: Machine Name.
- processor: Processor Type.
- maxram: Maximum RAM.
- year: Beginning Year of Production.
- model: Model Number.
- pic: Picture.

Note: Adding a new data category in database

Please manually update SpecsActivity, initCategory, activity_specs, String resources and Readme.

## In case of error

Please try deleting copied database in device under data directory.
