 ![icon](assets/logo.png)

Omni-Notes
==========


[![codebeat badge](https://codebeat.co/badges/d4322c00-0e03-4389-907c-4bf0e1606b68)](https://codebeat.co/projects/github-com-vexusia-omni-notes-classedits-develop)

Fork of omni-notes repo used solely for software quality classwork testing and modifications. This is not the official repo and should not be interpreted as such.

Link to official repo: https://github.com/federicoiosue/Omni-Notes


NOTE
==========
tl;dr - Replace dropbox api key with your own. Backup and restore module is now in zip form. Dropbox module only uploads/downloads zip backups. Must still use original backup and restore modules for making and restoring backups.


I was not expecting to have recieved emails from other developers interested in cloud storage on this app mainly because this was designed solely to be a class project and proof of concept so thankyou for your interest. Cloud storage is implemented in this but will require you to replace the DROPBOX API key in the codebase since it has been retired. The module for cloud storage is modified from the example code given by dropbox using ApiV2 and could use A LOT of cleaning up since there is some unused code left in. One of the prerequesities of implementation is that generating a backup and restoring has been changed to a single zip folder instead of the original backup output. Dropbox module is solely an intermediary that would require you to manually upload and download only zip files from your connected dropbox account. Generating backups and restoring backups must still be done from the original modules intended for this function. Dropbox only downloads onto local storage backup folder and uploads from it.






## License


    Copyright 2013-2018 Federico Iosue (original developer)
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.


[2]: https://crowdin.net/project/omni-notes/
[2]: https://crowdin.net/project/omni-notes/
