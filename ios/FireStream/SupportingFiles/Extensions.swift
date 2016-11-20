//
//  Extensions.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import Foundation
import UIKit

extension UIColor {
    static var colorPrimary: UIColor {
        return UIColor(colorLiteralRed: 255/255, green: 87/255, blue: 34/255, alpha: 1.0)
    }
    
    static var colorPrimaryDark: UIColor {
        return UIColor(colorLiteralRed: 230/255, green: 75/255, blue: 25/255, alpha: 1.0)
    }
    
    static var colorAccent: UIColor {
        return UIColor(colorLiteralRed: 33/255, green: 150/255, blue: 243/255, alpha: 1.0)
    }
    
    static var colorBg: UIColor {
        return UIColor(colorLiteralRed: 48/255, green: 48/255, blue: 48/255, alpha: 1.0)
    }
}

extension TimeInterval {
    var minuteSecondMS: String {
        return String(format:"%d:%02d.%03d", minute, second, millisecond)
    }
    var minuteSecond: String {
        return String(format:"%d:%02d", minute, second)
    }
    var minute: Int {
        return Int((self/60.0).truncatingRemainder(dividingBy: 60))
    }
    var second: Int {
        return Int(self.truncatingRemainder(dividingBy: 60))
    }
    var millisecond: Int {
        return Int((self*1000).truncatingRemainder(dividingBy: 1000))
    }
}

extension Int64 {
    var msToSeconds: Double {
        return Double(self) / 1000
    }
}

extension Collection where Indices.Iterator.Element == Index {
    
    /// Returns the element at the specified index iff it is within bounds, otherwise nil.
    subscript (safe index: Index) -> Generator.Element? {
        return indices.contains(index) ? self[index] : nil
    }
}
