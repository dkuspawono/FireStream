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
    }

}
