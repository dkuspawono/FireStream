//
//  Song.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import Foundation

class Song {
    var id: String = ""
    var name: String = ""
    var artist: String = ""
    var album: String = ""
    var albumUrl: String = ""
    var duration: Int64 = 0
    
    var dictionaryValue:[String:Any] {
        return [
            "id": id,
            "name": name,
            "artist": artist,
            "albumUrl": albumUrl,
            "duration": duration
        ]
    }
    
    init(dict: [String: Any]) {
        if let val = dict["id"] as? String {
            self.id = val
        }
        if let val = dict["name"] as? String {
            self.name = val
        }
        if let val = dict["artist"] as? String {
            self.artist = val
        }
        if let val = dict["album"] as? String {
            self.album = val
        }
        if let val = dict["albumUrl"] as? String {
            self.albumUrl = val
        }
        if let val = dict["duration"] as? Int64 {
            self.duration = val
        }
        if let val = dict["album"] as? [String:Any] {
            if let albumName = val["name"] as? String, album == "" {
                self.album = albumName
            }
            if let images = val["images"] as? [[String:Any]], albumUrl == "" {
                if let lastUrl = images.last?["url"] as? String {
                    self.albumUrl = lastUrl
                }
            }
        }
        if let val = (dict["artists"] as? [[String:Any]])?.first {
            if let artistName = val["name"] as? String, artist == "" {
                self.artist = artistName
            }
        }
    }

}
