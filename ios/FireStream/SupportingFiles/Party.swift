//
//  Party.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import Foundation

class Party {
    var id: String = ""
    var name: String = ""
    //var queue: [Song] = [Song]()
    //var requests: [Song] = [Song]()
    var hasPassword: Bool = false
    var hostId: String = ""
    var hostName: String = ""
    
    
    
    init(dict: [String: Any]) {
        if let val = dict["id"] as? String {
            self.id = val
        }
        if let val = dict["name"] as? String {
            self.name = val
        }
        if let val = dict["queue"] as? [[String:Any]] {
            
        }
        if let val = dict["requests"] as? [[String:Any]] {
            
        }
        if let val = dict["hasPassword"] as? Bool {
            self.hasPassword = val
        }
        if let val = dict["hostId"] as? String {
            self.hostId = val
        }
        if let val = dict["hostName"] as? String {
            self.hostName = val
        }
    }
}
