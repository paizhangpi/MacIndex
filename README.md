# MacIndex

Designed for quick vintage Macintosh computer specifications look-up on Android Devices.

## Information

This draft app is for education purpose, and internal use only. This app used to be part of University of Illinois CS125 FA19 Final Project.

If error happened, please try deleting copied database in device under data directory.

## Database Design Information

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
- year: Time of Introduction.
- model: Model Number.
- pic: Picture.

Note: Adding a new data category in database

Please manually update SpecsActivity, initCategory, activity_specs, String resources and Readme.
