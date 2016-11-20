//
//  SeekBar.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import UIKit

@IBDesignable
class SeekBar: UISlider {
    @IBInspectable var thumbImage: UIImage? {
        didSet {
            guard let image = thumbImage else { return }
            let newNormalImage = image.withSize(newSize: CGSize(width: 16, height: 16)).withRenderingMode(.alwaysTemplate)
            let newHightlightedImage = image.withSize(newSize: CGSize(width: 20, height: 20)).withRenderingMode(.alwaysTemplate)
            self.setThumbImage(newNormalImage, for: .normal)
            self.setThumbImage(newHightlightedImage, for: .highlighted)
        }
    }
}
