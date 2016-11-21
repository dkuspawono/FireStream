//
//  SongTableViewCell.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import UIKit
import Material

class SongTableViewCell: TableViewCell {
    @IBOutlet weak var lblName: UILabel!
    @IBOutlet weak var lblInfo: UILabel!
    @IBOutlet weak var imgAlbumArt: UIImageView!
    @IBOutlet weak var btnAdd: UIButton!
    @IBOutlet weak var constraintImageWidth: NSLayoutConstraint!
    @IBOutlet weak var constraintImageMargin: NSLayoutConstraint!
    @IBOutlet weak var constraintBtnWidth: NSLayoutConstraint!
    
    var showImage: Bool = true {
        didSet {
            constraintImageWidth.constant = showImage ? 64 : 0
            constraintImageMargin.constant = showImage ? 16 : 0
            imgAlbumArt.isHidden = !showImage
        }
    }
    
    var showBtnAdd: Bool = false {
        didSet {
            constraintBtnWidth.constant = showBtnAdd ? 32 : 0
            btnAdd.isHidden = !showBtnAdd
        }
    }

    override func awakeFromNib() {
        super.awakeFromNib()
        self.showBtnAdd = showBtnAdd ? true : false
        self.showImage = showImage ? true : false
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
